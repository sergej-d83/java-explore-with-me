package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    @GetMapping("/events")
    public ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(required = false) String text,
                                                         @RequestParam(required = false) Integer[] categories,
                                                         @RequestParam(required = false) Boolean isPaid,
                                                         @RequestParam(required = false) String rangeStart,
                                                         @RequestParam(required = false) String rangeEnd,
                                                         @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                         @RequestParam(required = false) String sort,
                                                         @PositiveOrZero
                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                         @Positive
                                                         @RequestParam(required = false, defaultValue = "10") Integer size,
                                                         HttpServletRequest request) {

        return new ResponseEntity<>(eventService.getEvents(text, categories, isPaid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request), HttpStatus.OK);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventFullDto> getPublicEventById(@PathVariable Long eventId, HttpServletRequest request) {

        return new ResponseEntity<>(eventService.getEventById(eventId, request), HttpStatus.OK);
    }
}
