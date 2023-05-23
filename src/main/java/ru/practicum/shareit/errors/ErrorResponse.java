package ru.practicum.shareit.errors;

import lombok.Data;

@Data
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
}
