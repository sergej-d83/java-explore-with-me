package ru.practicum.compilation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatingCompilationDto {

    private List<Long> eventIds;

    private Boolean isPinned;

    private String title;
}
