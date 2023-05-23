package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
public class UserDto {
    @PositiveOrZero(message = "id должен быть положительным")
    private long id;
    private String name;
    @Email(message = "Введите существующий адрес электронной почты!")
    private String email;
}
