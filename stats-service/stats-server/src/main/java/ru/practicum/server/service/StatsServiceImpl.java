package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.server.dto.EndpointHitDto;
import ru.practicum.server.dto.ViewStatsDto;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto addStats(EndpointHitDto endpointHitDto) {
        return EndpointHitMapper.toEndpointHitDto(statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {


        if (uris != null) {
            if (unique) {
                return statsRepository.getStatsByUrisAndIp(start, end, uris);
            } else {
                return statsRepository.getStatsByUris(start, end, uris);
            }
        } else {
            return statsRepository.getStatsByTime(start, end, unique);
        }
    }
}
