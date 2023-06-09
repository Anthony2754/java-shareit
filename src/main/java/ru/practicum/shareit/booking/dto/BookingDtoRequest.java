package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoRequest {
    @NotNull(message = "Id бронируемой вещи не может быть пустым")
    private Long itemId;
    @Future(message = "Дата начала бронирования должна быть в будущем")
    @NotNull(message = "Укажите дату начала бронирования")
    private LocalDateTime start;
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    @NotNull(message = "Укажите дату окончания бронирования")
    private LocalDateTime end;
}
