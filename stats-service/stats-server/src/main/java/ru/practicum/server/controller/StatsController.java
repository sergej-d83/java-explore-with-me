package ru.practicum.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.dto.EndpointHitDto;
import ru.practicum.server.dto.ViewStatsDto;
import ru.practicum.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    public EndpointHitDto addStats(@Valid @RequestBody EndpointHitDto endpointHitDto) {

        return statsService.addStats(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                       @RequestParam(value = "start") LocalDateTime start,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                       @RequestParam(value = "end") LocalDateTime end,
                                       @RequestParam(value = "uris") List<String> uris,
                                       @RequestParam(value = "unique", defaultValue = "false") Boolean unique) {

        return statsService.getStats(start, end, uris, unique);
    }
}
