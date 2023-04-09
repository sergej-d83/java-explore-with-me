package ru.practicum.errorhandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiError {

    private String status;

    private String reason;

    private String message;

    private LocalDateTime timestamp;
}
