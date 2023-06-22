package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    @JsonAlias(value = "itemId")
    @PositiveOrZero(message = "id должен быть положительным")
    private Long id;
    @NotBlank(message = "Заполните название")
    private String name;
    @NotBlank(message = "Заполните описание")
    private String description;
    @NotNull(message = "Доступность для бронирования должна быть заполнена")
    private Boolean available;
    private Long requestId;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private List<CommentDto> comments;
}
