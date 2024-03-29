package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.ParticipationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByEventIdAndIdInOrderById(Long eventId, List<Long> requestIds);

    List<ParticipationRequest> findAllByRequester(Long userId);

    Optional<ParticipationRequest> findByEventIdAndRequester(Long eventId, Long userId);
}
