package ru.practicum.comments.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class NewCommentDto {

    @NotBlank(message = "Комментарий не может быть пустым.")
    private String text;
}
