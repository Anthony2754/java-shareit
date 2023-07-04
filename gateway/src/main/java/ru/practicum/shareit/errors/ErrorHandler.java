package ru.practicum.shareit.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ru.practicum.shareit.errors.ErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e) {

        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.warn(exceptionName, e.getMessage());

        return new ResponseEntity<>(
                new ru.practicum.shareit.errors.ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ru.practicum.shareit.errors.ErrorResponse> handleConstraintViolationException(
            final ConstraintViolationException e) {

        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.warn(exceptionName, e.getMessage());

        return new ResponseEntity<>(
                new ru.practicum.shareit.errors.ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ru.practicum.shareit.errors.ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.warn(exceptionName, e.getMessage());

        return new ResponseEntity<>(
                new ru.practicum.shareit.errors.ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }
}
