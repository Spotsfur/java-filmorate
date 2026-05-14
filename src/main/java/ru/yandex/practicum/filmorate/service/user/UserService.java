package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.userStorage = storage;
    }

    public User create(User newUser) {
        return userStorage.create(newUser);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findOne(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос от пользователя {} на добавление в друзья {}", userId, friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Запрос от пользователя {} на удаление из друзей {}", userId, friendId);
        findById(userId);
        findById(friendId);
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> findUserFriends(Long userId) {
        log.info("Запрос списка друзей для пользователя {}", userId);
        findById(userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> findMutualFriends(Long userId, Long otherId) {
        log.info("Запрос общих друзей для пользователей {} и {}", userId, otherId);
        findById(userId);
        findById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}
