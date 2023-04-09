package ru.practicum.errorhandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "ru.practicum")
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Integrity constraint has been violated.",
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        return new ApiError(
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Integrity constraint has been violated.",
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequestException(InvalidRequestException e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerErrorException(final HttpServerErrorException.InternalServerError e) {
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getLocalizedMessage(),
                e.getMessage(),
                LocalDateTime.now()
        );
    }
}
