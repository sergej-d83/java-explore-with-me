package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.event.Location;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000, message = "Annotation minLength = 20, maxLength = 2000")
    private String annotation;

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(min = 20, max = 7000, message = "Description minLength = 20, maxLength = 7000")
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    @NotNull
    private Boolean isPaid;

    @NotNull
    @PositiveOrZero
    private Long participantLimit;

    @NotNull
    private Boolean requestModeration;

    @NotBlank
    @Size(min = 3, max = 120, message = "Title minLength = 3, maxLength = 120")
    private String title;
}
