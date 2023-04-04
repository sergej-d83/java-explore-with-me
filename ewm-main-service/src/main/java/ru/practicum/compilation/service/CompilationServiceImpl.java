package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.Compilation;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdatingCompilationDto;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий под номером " + compId + " не найдена."));

        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {

        List<Event> events;

        if (compilationDto.getEvents().isEmpty()) {
            events = Collections.emptyList();
        } else {
            events = eventRepository.findAllById(compilationDto.getEvents());
        }

        if (events.size() != compilationDto.getEvents().size()) {
            throw new NotFoundException("Одно или несколько событий небыли найдены.");
        }

        Compilation compilation = new Compilation();

        compilation.setEvents(events);
        compilation.setPinned(compilationDto.getPinned());
        compilation.setTitle(compilationDto.getTitle());

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long compId) {

        compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий под номером " + compId + " не найдена."));

        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdatingCompilationDto compilationDto) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий под номером " + compId + " не найдена."));

        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            if (events.size() != compilationDto.getEvents().size()) {
                throw new NotFoundException("Одно или несколько событий небыли найдены.");
            }
            compilation.setEvents(events);
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}
