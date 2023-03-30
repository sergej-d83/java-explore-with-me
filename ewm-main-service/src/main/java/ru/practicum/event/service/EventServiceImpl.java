package ru.practicum.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.event.EventSort;
import ru.practicum.event.QEvent;
import ru.practicum.event.dto.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.status.EventStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Value("${server.url}")
    private final String SERVER_URL;
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

        return eventRepository.findAll(query, pageable)
                .getContent()
                .stream().map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        return null;
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        return null;
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        return null;
    }

    @Override
    public List<EventFullDto> getEventsAsAdmin(Integer[] userIds, String[] states, Integer[] categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        return null;
    }

    @Override
    public EventFullDto updateEventAsAdmin(Long eventId, UpdateEventAdminRequest updateRequestDto) {
        return null;
    }

    @Override
    public EventFullDto getEventAsUser(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventFullDto updateEventAsUser(Long userId, Long eventId, UpdateEventUserRequest updatedEvent) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {
        return null;
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
}
