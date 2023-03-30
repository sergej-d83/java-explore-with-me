package ru.practicum.event;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.Category;
import ru.practicum.event.status.EventStatus;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.status.ParticipationRequestStatus;
import ru.practicum.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Embedded
    private Location location;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "participant_limit")
    private Long participantLimit;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference
    private List<ParticipationRequest> requests;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventStatus state;

    @Column(nullable = false)
    private String title;

    public List<ParticipationRequest> getConfirmedRequests() {
        if (this.requests.isEmpty()) {
            return Collections.emptyList();
        }

        return this.requests
                .stream()
                .filter((request) -> request.getStatus().equals(ParticipationRequestStatus.CONFIRMED))
                .collect(Collectors.toList());
    }
}
