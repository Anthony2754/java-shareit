package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = toUser(userDto.getId(), userDto);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Заполните адрес электронной почты");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Заполните имя");
        }
        User userStorage = userRepository.addUser(user);
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        User user = toUser(id, userDto);
        User userStorage = User.builder().build();
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            userStorage = userRepository.updateUserEmail(user);
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            userStorage = userRepository.updateUserName(user);
        }
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public UserDto getUserById(long userId) {
        User userStorage = userRepository.getUserById(userId);
        return toUserDto(userStorage.getId(), userStorage);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(user -> toUserDto(user.getId(), user))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(long userId) {
        userRepository.deleteUserById(userId);
    }
}
