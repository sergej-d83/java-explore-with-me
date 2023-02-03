package ru.practicum.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class EndpointHitDto {

    @NotNull(message = "Поле 'app' не может быть пустым.")
    private String app;

    @NotNull(message = "Поле 'uri' не может быть пустым.")
    private String uri;

    @NotNull(message = "Поле 'ip' не может быть пустым.")
    private String ip;

    @NotNull(message = "Поле 'timestamp' не может быть пустым.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
