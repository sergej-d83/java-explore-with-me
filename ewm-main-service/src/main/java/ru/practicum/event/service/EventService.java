package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(String text, Integer[] categories, Boolean isPaid, String rangeStart, String rangeEnd,
                                  Boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getEventsAsAdmin(Integer[] userIds, String[] states, Integer[] categories, String rangeStart,
                                        String rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAsAdmin(Long eventId, UpdateEventAdminRequest updateRequestDto);

    EventFullDto getEventAsUser(Long userId, Long eventId);

    EventFullDto updateEventAsUser(Long userId, Long eventId, UpdateEventUserRequest updatedEvent);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult  updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest);

}
