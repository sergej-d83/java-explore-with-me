package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.request.status.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;

    private Long eventId;

    private Long requesterId;

    private ParticipationRequestStatus status;
}
