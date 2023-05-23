package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.shareit.log.Logger.logChanges;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long id;

    @Override
    public User addUser(User user) {
        if (!users.containsKey(user.getId())) {
            checkExistEmail(user.getId(), user.getEmail());
            generateId();
            user.setId(id);
            users.put(user.getId(), user);
            logChanges("Добавлено", user.toString());
            return user;
        } else {
            throw new DuplicateException(String.format("Пользователь с id %s уже существует", user.getId()));
        }
    }

    @Override
    public User updateUserEmail(User user) {
        long userId = user.getId();
        checkUser(userId);
        checkExistEmail(userId, user.getEmail());
        users.get(userId).setEmail(user.getEmail());
        User userStorage = users.get(userId);
        logChanges("Обновлено", userStorage.toString());
        return userStorage;
    }

    @Override
    public User updateUserName(User user) {
        long userId = user.getId();
        checkUser(userId);
        users.get(userId).setName(user.getName());
        User userStorage = users.get(userId);
        logChanges("Обновлено", userStorage.toString());
        return userStorage;
    }

    @Override
    public User getUserById(long userId) {
        checkUser(userId);
        return users.get(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUserById(long userId) {
        checkUser(userId);
        users.remove(userId);
        logChanges("Удалено", String.format("Пользователь с id %s", userId));
    }

    private void checkExistEmail(long userId, String email) {
        for (User user : users.values()) {
            if (user.getId() != userId && user.getEmail().equals(email)) {
                throw new DuplicateException(String.format("Пользователь с email %s уже существует", email));
            }
        }
    }

    private void checkUser(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %s не найден", userId));
        }
    }

    private void generateId() {
        id++;
    }
}

