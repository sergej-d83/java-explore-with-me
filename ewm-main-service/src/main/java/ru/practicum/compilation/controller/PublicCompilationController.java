package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(@RequestParam(required = false) Boolean isPinned,
                                                                @PositiveOrZero
                                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                                @Positive
                                                                @RequestParam(required = false, defaultValue = "10") Integer size) {

        return new ResponseEntity<>(compilationService.getCompilations(isPinned, from, size), HttpStatus.OK);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable Long compId) {

        return new ResponseEntity<>(compilationService.getCompilation(compId), HttpStatus.OK);
    }
}
