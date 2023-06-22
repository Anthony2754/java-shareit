package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import java.util.Collection;

public interface UserService {

    UserDto addUserDto(UserDto userDto);

    UserDto getUserByIdDto(long userId);

    User getUserById(long userId);

    Collection<UserDto> getAllUsersDto();

    boolean userNotFound(long userId);

    UserDto updateUserDto(UserDto userDto, long userId);

    void deleteUserById(long userId);
}
