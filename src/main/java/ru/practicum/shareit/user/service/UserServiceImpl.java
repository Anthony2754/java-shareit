package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUseDto(UserDto userDto) {
        User user = userMapper.mapToUserModel(userDto);
        user.setId(null);

        try {
            user = userRepository.save(user);
            log.debug("Добавлен новый пользователь: {}", user);
            return userMapper.mapToUserDto(user);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Ошибка добавления пользователя: такой email уже зарегистрирован");
        }

    }

    @Override
    public UserDto getUserByIdDto(long userId) {

        return userMapper.mapToUserDto(this.getUserById(userId));
    }

    @Override
    public User getUserById(long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(
                    String.format("Ошибка получения: пользователь с id=%d не найден", userId));
        }

        return user.get();
    }

    @Override
    public Collection<UserDto> getAllUsersDto() {

        return userRepository.findAll().stream()
                .map(userMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userNotFound(long userId) {
        return !userRepository.existsById(userId);
    }

    @Override
    public UserDto updateUserDto(UserDto userDto, long userId) {
        Map<UserUpdateFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        User user;

        if (userDto.getName() != null) {
            targetFields.put(UserUpdateFields.NAME, true);
            empty = false;
        } else {
            targetFields.put(UserUpdateFields.NAME, false);
        }

        if (userDto.getEmail() != null) {
            targetFields.put(UserUpdateFields.EMAIL, true);
            empty = false;
        } else {
            targetFields.put(UserUpdateFields.EMAIL, false);
        }

        if (empty) {
            throw new ValidationException("Ошибка обновления пользователя: в запросе все поля равны null");
        }

        user = userMapper.mapToUserModel(userDto);
        user.setId(userId);

        try {
            user = userRepository.updateUser(user, targetFields);

            log.debug("Обновлен пользователь: {}", user);
            return userMapper.mapToUserDto(user);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Ошибка обновления пользователя: такой email уже зарегистрирован");
        }
    }

    @Override
    public void deleteUserById(long userId) {
        if (userRepository.existsById(userId)) {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                itemRepository.deleteAllByOwner(userOptional.get());

            } else throw new RuntimeException();
            userRepository.deleteById(userId);

        } else throw new NotFoundException(String.format("Ошибка удаления: пользователь с id=%d не найден", userId));
    }
}
