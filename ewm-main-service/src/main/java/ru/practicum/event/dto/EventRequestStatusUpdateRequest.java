package ru.practicum.event.dto;

import lombok.Data;
import ru.practicum.request.status.ParticipationRequestStatus;

@Data
public class EventRequestStatusUpdateRequest {

    private Long[] requestIds;

    private ParticipationRequestStatus status;
}
