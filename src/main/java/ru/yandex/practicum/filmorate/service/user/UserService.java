package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.userStorage = storage;
    }

    public User create(User newUser) {
        log.info("Добавляем нового пользователя");
        createValidation(newUser);
        if (newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        return userStorage.create(newUser);
    }

    public User update(User newUser) {
        log.info("Изменяем пользователя");
        //Отсюда валятся эксепшны
        if (newUser.getId() == null) {
            throw new ValidationException("id должен быть указан");
        }
        findById(newUser.getId());
        User oldUser = updateValidation(newUser);
        User nullReplacedUser = replaceNullDataUser(newUser, oldUser);
        return userStorage.update(nullReplacedUser);
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
        if (!userStorage.existsById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            log.warn("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> findUserFriends(Long userId) {
        log.info("Запрос списка друзей для пользователя {}", userId);
        if (!userStorage.existsById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return userStorage.getFriends(userId);
    }

    public Collection<User> findMutualFriends(Long userId, Long otherId) {
        log.info("Запрос общих друзей для пользователей {} и {}", userId, otherId);
        if (!userStorage.existsById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existsById(otherId)) {
            log.warn("Пользователь с id {} не найден", otherId);
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void createValidation(User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || newUser.getEmail().indexOf('@') == -1) {
            log.warn("Попытка добавления пользователя с неправильной электронной почтой");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().indexOf(' ') >= 0) {
            log.warn("Попытка добавления пользователя с неправильным логином");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Попытка добавления пользователя с неправильной датой рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private User updateValidation(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Попытка изменения пользователя с неправильным id");
            throw new ValidationException("id должен быть указан");
        }
        User oldUser = userStorage.findOne(newUser.getId())
                .orElseThrow(() -> {
                    log.warn("Попытка изменить пользователя, id которого не существует в базе");
                    return new ValidationException("Пользователь с id " + newUser.getId() + " не найден");
                });
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank() || newUser.getEmail().indexOf('@') == -1) {
                log.warn("Попытка изменения электронной почты пользователя на неправильное");
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().indexOf(' ') >= 0) {
                log.warn("Попытка изменения логина пользователя на неправильное");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Попытка изменения даты рождения пользователя на дату в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
        return oldUser;
    }

    private User replaceNullDataUser(User newUser, User oldUser) {
        if (newUser.getEmail() == null) {
            newUser.setEmail(oldUser.getEmail());
        }
        if (newUser.getBirthday() == null) {
            newUser.setBirthday(oldUser.getBirthday());
        }
        if (newUser.getLogin() == null) {
            newUser.setLogin(oldUser.getLogin());
        }
        if (newUser.getName() == null) {
            newUser.setName(oldUser.getName());
        } else if (newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        return newUser;
    }
}
