package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User addUser(User user);

    User updateUserEmail(User user);

    User updateUserName(User user);

    User getUserById(long userId);

    List<User> getAllUsers();

    void deleteUserById(long userId);
}
