package ru.practicum.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.server.dto.EndpointHitDto;
import ru.practicum.server.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsClient {

    @Value("${stats-server.url}")
    private final String serverUrl;

    private final RestTemplate restTemplate;

    public void addStats(EndpointHitDto endpointHitDto) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);

        restTemplate.exchange(serverUrl + "/hit", HttpMethod.POST, requestEntity, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique)
            throws JsonProcessingException {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("uris", uris);
        parameters.put("unique", unique);

        ResponseEntity<String> response = restTemplate.getForEntity(
                serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                String.class, parameters);

        ObjectMapper objectMapper = new ObjectMapper();
        ViewStatsDto[] array;

        array = objectMapper.readValue(response.getBody(), ViewStatsDto[].class);

        return Arrays.asList(array);
    }
}