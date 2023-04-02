package ru.practicum.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.event.Event;
import ru.practicum.event.EventSort;
import ru.practicum.event.QEvent;
import ru.practicum.event.dto.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.status.AdminStateAction;
import ru.practicum.event.status.EventStatus;
import ru.practicum.event.status.UserStateAction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.status.ParticipationRequestStatus;
import ru.practicum.server.dto.EndpointHitDto;
import ru.practicum.user.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    @Value("${server.url}")
    private String SERVER_URL;
    private final StatsClient statsClient;

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventShortDto> getEvents(String text, Integer[] categories, Boolean isPaid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                         Integer size, HttpServletRequest request) {

        Sort sorting = EventSort.valueOf(sort).equals(EventSort.VIEWS) ?
                Sort.by(Sort.Direction.DESC, "views") :
                Sort.by(Sort.Direction.ASC, "eventDate");

        Pageable pageable = PageRequest.of(from / size, size, sorting);

        BooleanExpression query = prepareQueryForUserRequest(text, categories, isPaid, rangeStart, rangeEnd, onlyAvailable);

        List<EventShortDto> events = eventRepository.findAll(query, pageable)
                .getContent()
                .stream().map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        sendStatistics(request);

        return events;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (!event.getState().equals(EventStatus.PUBLISHED)) {
            throw new NotFoundException("Событие под номером " + eventId + " не найдено.");
        }
        sendStatistics(request);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size));

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {

        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Неправильное время события. " + newEventDto.getEventDate());
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategoryId()).orElseThrow(
                () -> new NotFoundException("Категория под номером " + newEventDto.getCategoryId() + " не найдена"));

        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEventsAsAdmin(Integer[] userIds, String[] states, Integer[] categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {

        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
        Pageable pageable = PageRequest.of(from / size, size, sort);

        BooleanExpression query = prepareQueryForAdminRequest(userIds, states, categories, rangeStart, rangeEnd);

        return eventRepository.findAll(query, pageable)
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventAsAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Начало события меньше чем через час.");
        }

        if (updateRequest.getStateAction() != null) {

            if (event.getState() != EventStatus.PENDING &&
                    updateRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                throw new ConflictException("Событие уже опубликовано или отклонено.");
            }
            if (event.getState() == EventStatus.PUBLISHED &&
                    updateRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                throw new ConflictException("Нельзя отклонить уже опубликованное событие.");
            }
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена.")));
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getIsPaid() != null) {
            event.setIsPaid(updateRequest.getIsPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                event.setState(EventStatus.PUBLISHED);
            }
            if (updateRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                event.setState(EventStatus.CANCELED);
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventAsUser(Long userId, Long eventId) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventAsUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие под номером " + eventId + " не найдено.");
        }
        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ConflictException("Нельзя обновить опубликованное событие");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Событие начнётся раньше чем через 2 часа.");
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена.")));
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getIsPaid() != null) {
            event.setIsPaid(updateRequest.getIsPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventStatus.PENDING);
            }
            if (updateRequest.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventStatus.CANCELED);
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new NotFoundException("Событие под номером " + eventId + " не найдено.");
        }

        return event.getRequests().stream().map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new NotFoundException("Событие под номером " + eventId + " не найдено.");
        }

        List<ParticipationRequest> requests = updateStatusOfRequests(event, statusUpdateRequest);
        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedRequests = requests.stream()
                .filter(request -> request.getStatus().equals(ParticipationRequestStatus.CONFIRMED))
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedRequests = requests.stream()
                .filter(request -> request.getStatus().equals(ParticipationRequestStatus.REJECTED))
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

    private BooleanExpression prepareQueryForUserRequest(String text, Integer[] categories, Boolean isPaid,
                                                         String rangeStart, String rangeEnd, Boolean onlyAvailable) {

        QEvent qEvent = QEvent.event;
        BooleanExpression query = qEvent.state.eq(EventStatus.PUBLISHED);

        if (text != null) {
            query.and(qEvent.annotation.containsIgnoreCase(text).or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categories != null) {
            query.and(qEvent.category.id.in(categories));
        }
        if (isPaid != null) {
            query.and(qEvent.isPaid.eq(isPaid));
        }
        if (rangeStart != null) {
            query.and(qEvent.eventDate.after(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)));
        }
        if (rangeEnd != null) {
            query.and(qEvent.eventDate.before(LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER)));
        }
        if (rangeStart == null) {
            query.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        if (onlyAvailable) {
            query.and(qEvent.requests.size().lt(qEvent.participantLimit));
        }

        return query;
    }

    private BooleanExpression prepareQueryForAdminRequest(Integer[] userIds, String[] states, Integer[] categories,
                                                          String rangeStart, String rangeEnd) {

        QEvent qEvent = QEvent.event;
        BooleanExpression query = qEvent.id.isNotNull();

        if (userIds != null && userIds.length > 0) {
            query.and(qEvent.initiator.id.in(userIds));
        }
        if (states != null && states.length > 0) {

            List<EventStatus> list = Arrays.stream(states).map(EventStatus::valueOf).collect(Collectors.toList());
            query.and(qEvent.state.in(list));
        }
        if (categories != null && categories.length > 0) {
            query.and(qEvent.category.id.in(categories));
        }
        if (rangeStart != null) {
            query.and(qEvent.eventDate.after(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)));
        }
        if (rangeEnd != null) {
            query.and(qEvent.eventDate.before(LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER)));
        }

        return query;
    }

    private void sendStatistics(HttpServletRequest request) {

        statsClient.setServerUrl(SERVER_URL);

        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp(request.getHeader("User-Agent"));
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());

        statsClient.addStats(endpointHitDto);
    }

    private List<ParticipationRequest> updateStatusOfRequests(Event event, EventRequestStatusUpdateRequest updateRequest) {

        int confirmed = event.getConfirmedRequests().size();
        long participantLimit = event.getParticipantLimit();

        if (confirmed >= participantLimit) {
            throw new ConflictException("Лимит участников достигнут.");
        }

        List<ParticipationRequest> requests = requestRepository.
                findByEventIdAndIdInOrderById(event.getId(), List.of(updateRequest.getRequestIds()));

        for (ParticipationRequest request : requests) {

            if (!request.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ConflictException("Запрос на участие должен быть в стадии ожидания.");
            }

            if (updateRequest.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
                if ((participantLimit == 0 || !event.getRequestModeration()) && (confirmed < participantLimit)) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmed++;

                } else if ((!event.getRequestModeration() && participantLimit > 0) && (confirmed > participantLimit)) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmed++;

                } else if (confirmed < participantLimit) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmed++;

                } else {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                }
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
            }
        }
        return requests;
    }
}
