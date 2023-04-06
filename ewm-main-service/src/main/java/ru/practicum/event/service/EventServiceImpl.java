package ru.practicum.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.status.ParticipationRequestStatus;
import ru.practicum.server.dto.EndpointHitDto;
import ru.practicum.server.dto.ViewStatsDto;
import ru.practicum.user.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

        Sort sorting = Sort.by(Sort.Direction.ASC, "eventDate");

        if (sort != null && !sort.isBlank() && EventSort.VIEWS.equals(EventSort.valueOf(sort))) {
            sorting = Sort.by(Sort.Direction.DESC, "views");
        }

        Pageable pageable = PageRequest.of(from / size, size, sorting);

        BooleanExpression query = prepareQueryForUserRequest(text, categories, isPaid, rangeStart, rangeEnd, onlyAvailable);

        List<Event> events = eventRepository.findAll(query, pageable).getContent();

        sendStatistics(request);
        getViews(events);

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (!event.getState().equals(EventStatus.PUBLISHED)) {
            throw new NotFoundException("Событие под номером " + eventId + " не найдено.");
        }
        sendStatistics(request);
        getViews(List.of(event));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size));

        getViews(events);

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {

        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Неправильное время события. " + newEventDto.getEventDate());
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                () -> new InvalidRequestException("Категория под номером " + newEventDto.getCategory() + " не найдена"));

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

        List<Event> events = eventRepository.findAll(query, pageable).getContent();

        getViews(events);

        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventAsAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
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
        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateRequest.getCategory())
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
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
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
                event.setPublishedOn(LocalDateTime.now());
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

        getViews(List.of(event));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventAsUser(Long userId, Long eventId, UpdateEventUserRequest userUpdateRequest) {

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
        if (userUpdateRequest.getEventDate() != null) {
            if (userUpdateRequest.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ConflictException("Начало события не может быть в прошлом. " + userUpdateRequest.getEventDate());
            }
        }

        if (userUpdateRequest.getAnnotation() != null) {
            event.setAnnotation(userUpdateRequest.getAnnotation());
        }
        if (userUpdateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(userUpdateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена.")));
        }
        if (userUpdateRequest.getDescription() != null) {
            event.setDescription(userUpdateRequest.getDescription());
        }
        if (userUpdateRequest.getEventDate() != null) {
            event.setEventDate(userUpdateRequest.getEventDate());
        }
        if (userUpdateRequest.getLocation() != null) {
            event.setLocation(userUpdateRequest.getLocation());
        }
        if (userUpdateRequest.getPaid() != null) {
            event.setPaid(userUpdateRequest.getPaid());
        }
        if (userUpdateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(userUpdateRequest.getParticipantLimit());
        }
        if (userUpdateRequest.getRequestModeration() != null) {
            event.setRequestModeration(userUpdateRequest.getRequestModeration());
        }
        if (userUpdateRequest.getStateAction() != null) {
            if (userUpdateRequest.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventStatus.PENDING);
            }
            if (userUpdateRequest.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventStatus.CANCELED);
            }
        }
        if (userUpdateRequest.getTitle() != null) {
            event.setTitle(userUpdateRequest.getTitle());
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

    private BooleanExpression prepareQueryForUserRequest(String text, Integer[] categories, Boolean paid,
                                                         String rangeStart, String rangeEnd, Boolean onlyAvailable) {

        QEvent qEvent = QEvent.event;
        BooleanExpression query = qEvent.state.eq(EventStatus.PUBLISHED);

        if (text != null) {
            query = query.and(qEvent.annotation.containsIgnoreCase(text).or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categories != null) {
            query = query.and(qEvent.category.id.in(categories));
        }
        if (paid != null) {
            query = query.and(qEvent.paid.eq(paid));
        }
        if (rangeStart != null) {
            query = query.and(qEvent.eventDate.after(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)));
        }
        if (rangeEnd != null) {
            query = query.and(qEvent.eventDate.before(LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER)));
        }
        if (rangeStart == null) {
            query = query.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        if (onlyAvailable) {
            query = query.and(qEvent.requests.size().lt(qEvent.participantLimit));
        }

        return query;
    }

    private BooleanExpression prepareQueryForAdminRequest(Integer[] userIds, String[] states, Integer[] categories,
                                                          String rangeStart, String rangeEnd) {

        QEvent qEvent = QEvent.event;
        BooleanExpression query = qEvent.id.isNotNull();

        if (userIds != null && userIds.length > 0) {
            query = query.and(qEvent.initiator.id.in(userIds));
        }
        if (states != null && states.length > 0) {

            List<EventStatus> list = Arrays.stream(states).map(EventStatus::valueOf).collect(Collectors.toList());
            query = query.and(qEvent.state.in(list));
        }
        if (categories != null && categories.length > 0) {
            query = query.and(qEvent.category.id.in(categories));
        }
        if (rangeStart != null) {
            query = query.and(qEvent.eventDate.after(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)));
        }
        if (rangeEnd != null) {
            query = query.and(qEvent.eventDate.before(LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER)));
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

        int countConfirmed = 0;

        if (event.getConfirmedRequests().size() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут.");
        }

        List<ParticipationRequest> requests = requestRepository.
                findByEventIdAndIdInOrderById(event.getId(), List.of(updateRequest.getRequestIds()));

        for (ParticipationRequest request : requests) {

            if (!request.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ConflictException("Запрос на участие должен быть в стадии ожидания.");
            }

            if (updateRequest.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
                if ((event.getParticipantLimit() == 0 || !event.getRequestModeration()) &&
                        (event.getConfirmedRequests().size() + countConfirmed < event.getParticipantLimit())) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    countConfirmed++;

                } else if ((!event.getRequestModeration() && event.getParticipantLimit() > 0) &&
                        (event.getConfirmedRequests().size() + countConfirmed < event.getParticipantLimit())) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    countConfirmed++;

                } else if (event.getConfirmedRequests().size() + countConfirmed < event.getParticipantLimit()) {

                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    countConfirmed++;

                } else {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                }
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
            }
        }
        return requests;
    }



    @SneakyThrows
    private void getViews(List<Event> events) {

        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        List<String> uris = new ArrayList<>();

        for (Event event : events) {
            uris.add("/events/" + event.getId().toString());
        }

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, false);


        for (Event event : events) {
            for (ViewStatsDto stat : stats) {
                if (stat.getUri().equals("/events/" + event.getId().toString())) {
                    event.setViews(event.getViews() + 1);
                }
            }
        }
    }
}
