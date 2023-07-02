package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserUpdateFields;

import java.util.Map;

@Repository
public interface CustomUserRepository {
    User updateUser(User user, Map<UserUpdateFields, Boolean> targetFields);
}
