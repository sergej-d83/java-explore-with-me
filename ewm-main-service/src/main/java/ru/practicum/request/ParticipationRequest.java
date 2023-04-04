package ru.practicum.request;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import ru.practicum.event.Event;
import ru.practicum.request.status.ParticipationRequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    @Column(name = "requester_id", nullable = false)
    private Long requester;

    @Enumerated(EnumType.STRING)
    private ParticipationRequestStatus status;

}
