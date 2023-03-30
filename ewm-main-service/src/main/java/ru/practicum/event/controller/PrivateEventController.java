package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUserId(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {

        return new ResponseEntity<>(eventService.getEventsByUserId(userId, from, size), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return new ResponseEntity<>(eventService.addEvent(userId, newEventDto), HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventAsUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return new ResponseEntity<>(eventService.getEventAsUser(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAsUser(@RequestBody UpdateEventUserRequest updatedEvent,
                                                    @PathVariable Long userId, @PathVariable Long eventId) {

        return new ResponseEntity<>(eventService.updateEventAsUser(userId, eventId, updatedEvent), HttpStatus.OK);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable Long userId,
                                                                          @PathVariable Long eventId) {

        return new ResponseEntity<>(eventService.getEventRequests(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestStatus(
            @PathVariable Long userId, @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {

        return new ResponseEntity<>(eventService.updateRequestStatus(userId, eventId, statusUpdateRequest), HttpStatus.OK);
    }
}
