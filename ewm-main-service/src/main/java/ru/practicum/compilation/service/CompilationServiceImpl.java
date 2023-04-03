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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean isPinned, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Compilation> compilations = compilationRepository.findAllByIsPinned(isPinned, pageable);

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

        List<Event> events = eventRepository.findAllById(compilationDto.getEventIds());
        if (events.size() != compilationDto.getEventIds().size()) {
            throw new NotFoundException("Одно или несколько событий небыли найдены.");
        }

        Compilation compilation = new Compilation();

        compilation.setEvents(events);
        compilation.setIsPinned(compilationDto.getIsPinned());
        compilation.setTitle(compilation.getTitle());

        return CompilationMapper.toCompilationDto(compilation);
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
        if (compilationDto.getIsPinned() != null) {
            compilation.setIsPinned(compilationDto.getIsPinned());
        }
        if (compilationDto.getEventIds() != null) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEventIds());
            if (events.size() != compilationDto.getEventIds().size()) {
                throw new NotFoundException("Одно или несколько событий небыли найдены.");
            }
            compilation.setEvents(events);
        }

        return CompilationMapper.toCompilationDto(compilation);
    }
}
