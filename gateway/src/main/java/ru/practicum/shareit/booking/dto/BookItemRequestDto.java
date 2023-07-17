package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookItemRequestDto {
    @NotNull(message = "Id бронируемой вещи не может быть пустым.")
    private long itemId;
    @FutureOrPresent(message = "Дата начала бронирования должна быть в будущем.")
    @NotNull(message = "Укажите дату начала бронирования.")
    private LocalDateTime start;
    @Future(message = "Дата окончания бронирования должна быть в будущем.")
    @NotNull(message = "Укажите дату окончания бронирования.")
    private LocalDateTime end;
}
