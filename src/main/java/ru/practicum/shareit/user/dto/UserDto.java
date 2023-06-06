package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.user.controller.UserValidation;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @PositiveOrZero(message = "id должен быть положительным")
    private Long id;
    @NotNull(message = "Заполните адрес электронной почты", groups = UserValidation.FullValidation.class)
    @Email(message = "Введите существующий адрес электронной почты",
            groups = {UserValidation.FullValidation.class, UserValidation.PartialValidation.class})
    private String email;
    @NotBlank(message = "Введите имя пользователя", groups = UserValidation.FullValidation.class)
    private String name;
}
