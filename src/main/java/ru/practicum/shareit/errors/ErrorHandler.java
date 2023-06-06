package ru.practicum.shareit.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.WrongOwnerItemException;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ErrorResponse> handleValidationException(final ValidationException e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        int start = exceptionMessage.lastIndexOf(":") + 2;
        exceptionMessage = e.getMessage().substring(start);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, exceptionMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(WrongOwnerItemException.class)
    ResponseEntity<ErrorResponse> handleForbiddenExceptions(final RuntimeException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFoundExceptions(final NotFoundException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DuplicateException.class)
    ResponseEntity<ErrorResponse> handleConflictExceptions(final DuplicateException e) {
        String exceptionName = e.getClass().getName();
        exceptionName = exceptionName.substring(exceptionName.lastIndexOf(".") + 1);
        log.debug(e.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse(exceptionName, e.getMessage()),
                HttpStatus.CONFLICT
        );
    }
}
