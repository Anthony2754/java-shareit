package ru.practicum.shareit.user.repository;

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserUpdateFields;

import java.util.Map;
import java.util.Optional;

@Transactional
public class UserRepositoryImpl implements CustomUserRepository {

    private final UserRepository userRepository;

    public UserRepositoryImpl(@Lazy UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User updateUser(User user, Map<UserUpdateFields, Boolean> targetFields) {
        long id = user.getId();
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User userExisting = userOptional.get();
            String name = user.getName();
            String email = user.getEmail();
            User newUser;

            if (!targetFields.get(UserUpdateFields.NAME)) {
                name = userExisting.getName();
            }
            if (!targetFields.get(UserUpdateFields.EMAIL)) {
                email = userExisting.getEmail();
            }
            newUser = User.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .build();

            return userRepository.save(newUser);

        } else throw new NotFoundException(
                String.format("Ошибка обновления: пользователь с id=%d не найден.", id));
    }
}
