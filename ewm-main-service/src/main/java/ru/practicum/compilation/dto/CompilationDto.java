package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {

    private Long id;

    private Boolean isPinned;

    private String title;

    private List<EventShortDto> events;
}
