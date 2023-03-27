package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/admin/events")
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) Integer[] userIds,
                                                        @RequestParam(required = false) String[] states,
                                                        @RequestParam(required = false) Integer[] categories,
                                                        @RequestParam(required = false) String rangeStart,
                                                        @RequestParam(required = false) String rangeEnd,
                                                        @PositiveOrZero
                                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                                        @Positive
                                                        @RequestParam(required = false, defaultValue = "10") Integer size) {

        return new ResponseEntity<>(eventService.getEvents(userIds, states, categories,
                rangeStart, rangeEnd, from, size), HttpStatus.OK);
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@RequestBody UpdateEventAdminRequestDto updateRequestDto,
                                                    @PathVariable Long eventId) {

        return new ResponseEntity<>(eventService.updateByAdmin(eventId, updateRequestDto), HttpStatus.OK);
    }
}
