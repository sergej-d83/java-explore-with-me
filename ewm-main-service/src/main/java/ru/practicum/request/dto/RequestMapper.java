package ru.practicum.request.dto;

import ru.practicum.request.ParticipationRequest;

public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(ParticipationRequest request) {

        ParticipationRequestDto requestDto = new ParticipationRequestDto();

        requestDto.setId(request.getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setEventId(request.getEvent().getId());
        requestDto.setRequesterId(request.getRequesterId());
        requestDto.setStatus(request.getStatus());

        return requestDto;
    }
}
