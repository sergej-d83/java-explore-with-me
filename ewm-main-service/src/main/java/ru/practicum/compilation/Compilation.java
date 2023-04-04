package ru.practicum.compilation;

import lombok.Data;
import ru.practicum.event.Event;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "compilations")
@Data
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_pinned", nullable = false)
    private Boolean pinned;

    @Column(nullable = false)
    private String title;

    @ManyToMany
    @JoinTable(name = "event_compilations",
    joinColumns = @JoinColumn(name = "compilation_id"),
    inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;
}
