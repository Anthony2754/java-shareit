package ru.practicum.shareit.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String errorName;
    private final String error;
}
