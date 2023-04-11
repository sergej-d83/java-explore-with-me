package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.status.EventStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.status.ParticipationRequestStatus;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {


    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        List<ParticipationRequest> requests = requestRepository.findAllByRequester(userId);

        return requests.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {

        Optional<ParticipationRequest> optionalRequest = requestRepository.findByEventIdAndRequester(eventId, userId);
        if (optionalRequest.isPresent()) {
            throw new ConflictException("Запрос на участие в событии уже существует.");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (event.getInitiator().getId() != null && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя делать запрос на участие в своём событии.");
        }

        if (!EventStatus.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Событие ещё не опубликовано.");
        }

        if (event.getConfirmedRequests().size() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут");
        }

        ParticipationRequest request = new ParticipationRequest();

        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(userId);
        if (event.getRequestModeration()) {
            request.setStatus(ParticipationRequestStatus.PENDING);
        } else {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        }

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос не номер " + requestId + " не найден."));

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден"));

        if (request.getRequester() != null && request.getRequester().equals(userId)) {
            request.setStatus(ParticipationRequestStatus.CANCELED);
        } else {
            throw new ConflictException("Можно отклонить только свой запрос.");
        }

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }
}
