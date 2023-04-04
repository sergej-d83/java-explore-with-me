package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.server.dto.ViewStatsDto;
import ru.practicum.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query(value = "select h.app as app, h.uri as uri, count(h.ip) as hits " +
            "from hits as h " +
            "where uri in (:uris) and h.request_time between (:start) and (:end) " +
            "group by h.ip, h.uri, h.app " +
            "order by hits desc", nativeQuery = true)
    List<ViewStatsDto> getStatsByUrisAndIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select h.app as app, h.uri as uri, count(*) as hits " +
            "from hits as h " +
            "where uri in (:uris) and h.request_time between (:start) and (:end) " +
            "group by h.uri, h.app " +
            "order by hits desc", nativeQuery = true)
    List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select h.app as app, h.uri as uri, " +
            "(case when :unique = true then count(distinct h.ip) else count(h.ip) end) as hits " +
            "from hits as h " +
            "where h.request_time between (:start) and (:end) " +
            "group by h.uri, h.app " +
            "order by hits desc", nativeQuery = true)
    List<ViewStatsDto> getStatsByTime(LocalDateTime start, LocalDateTime end, Boolean unique);
}
