package ru.practicum.compilation.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class NewCompilationDto {

    private List<Long> eventIds;

    @NotNull
    private Boolean isPinned;

    @NotBlank
    private String title;
}
