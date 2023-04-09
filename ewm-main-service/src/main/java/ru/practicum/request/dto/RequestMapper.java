package ru.practicum.request.dto;

import ru.practicum.request.ParticipationRequest;

public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(ParticipationRequest request) {

        ParticipationRequestDto requestDto = new ParticipationRequestDto();

        requestDto.setId(request.getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setEvent(request.getEvent().getId());
        requestDto.setRequester(request.getRequester());
        requestDto.setStatus(request.getStatus());

        return requestDto;
    }
}
