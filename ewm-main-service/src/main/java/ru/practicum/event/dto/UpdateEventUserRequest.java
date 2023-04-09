package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.event.Location;
import ru.practicum.event.status.UserStateAction;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Annotation minLength = 20, maxLength = 2000")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Description minLength = 20, maxLength = 7000")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    private Long participantLimit;

    private Boolean requestModeration;

    private UserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Title minLength = 3, maxLength = 120")
    private String title;
}
