package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    @PositiveOrZero(message = "id должен быть положительным")
    private Long id;
    @NotBlank(message = "Заполните название")
    private String name;
    @NotBlank(message = "Заполните описание")
    private String description;
    @NotNull(message = "Доступность для бронирования должна быть заполнена")
    private Boolean available;
    private Long requestId;
}
