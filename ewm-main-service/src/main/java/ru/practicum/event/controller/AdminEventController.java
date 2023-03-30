package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/admin/events")
    public ResponseEntity<List<EventFullDto>> getEventsAsAdmin(@RequestParam(required = false) Integer[] userIds,
                                                               @RequestParam(required = false) String[] states,
                                                               @RequestParam(required = false) Integer[] categories,
                                                               @RequestParam(required = false) String rangeStart,
                                                               @RequestParam(required = false) String rangeEnd,
                                                               @PositiveOrZero
                                                               @RequestParam(required = false, defaultValue = "0") Integer from,
                                                               @Positive
                                                               @RequestParam(required = false, defaultValue = "10") Integer size) {

        return new ResponseEntity<>(eventService.getEventsAsAdmin(userIds, states, categories,
                rangeStart, rangeEnd, from, size), HttpStatus.OK);
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAsAdmin(@RequestBody UpdateEventAdminRequest updateRequestDto,
                                                           @PathVariable Long eventId) {

        return new ResponseEntity<>(eventService.updateEventAsAdmin(eventId, updateRequestDto), HttpStatus.OK);
    }
}
