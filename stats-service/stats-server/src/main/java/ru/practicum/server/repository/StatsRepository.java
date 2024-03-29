package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.server.dto.ViewStatsDto;
import ru.practicum.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.server.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit as h " +
            "where h.uri in (:uris) and h.timestamp between (:start) and (:end) " +
            "group by h.ip, h.uri, h.app " +
            "order by count(h.id) desc")
    List<ViewStatsDto> getStatsByUrisAndIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.server.dto.ViewStatsDto(h.app, h.uri, count(h.id))" +
            "from EndpointHit as h " +
            "where h.uri in (:uris) and h.timestamp between (:start) and (:end) " +
            "group by h.uri, h.app " +
            "order by count(h.id) desc")
    List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.server.dto.ViewStatsDto(h.app, h.uri, count(h.id)) " +
            "from EndpointHit as h " +
            "where h.timestamp between (:start) and (:end) " +
            "group by h.uri, h.app " +
            "order by count(h.id) desc")
    List<ViewStatsDto> getStatsByTime(LocalDateTime start, LocalDateTime end);
}
