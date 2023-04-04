package ru.practicum.compilation.dto;

import ru.practicum.compilation.Compilation;
import ru.practicum.event.dto.EventMapper;

import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation) {

        CompilationDto compilationDto = new CompilationDto();

        compilationDto.setId(compilation.getId());
        compilationDto.setEvents(compilation.getEvents()
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList()));
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());

        return compilationDto;
    }
}
