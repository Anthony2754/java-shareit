package ru.practicum.shareit.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Objects;

import static ru.practicum.shareit.log.Logger.logExceptionWarning;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    public ErrorResponse handleValidationException(Exception e) {
        logExceptionWarning(e);
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException eValidation = (MethodArgumentNotValidException) e;
            message = Objects.requireNonNull(eValidation.getBindingResult().getFieldError()).getDefaultMessage();
        } else {
            message = e.getMessage();
        }
        return new ErrorResponse(400, "Bad Request", message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        logExceptionWarning(e);
        return new ErrorResponse(404, "Not Found", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorResponse handleConflictException(DuplicateException e) {
        logExceptionWarning(e);
        return new ErrorResponse(409, "Conflict", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        logExceptionWarning(e);
        return new ErrorResponse(500, "Internal Server Error", "Произошла непредвиденная ошибка.");
    }
}
