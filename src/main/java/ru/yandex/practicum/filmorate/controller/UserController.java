package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    //Создаём пользователя
    @PostMapping
    public User create(@RequestBody User newUser) {
        log.info("Добавляем нового пользователя");
        //Проверки
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
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    //Обновляем пользователя
    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Изменяем пользователя");
        if (newUser.getId() == null) {
            log.warn("Попытка изменения пользователя с неправильным id");
            throw new ValidationException("id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            //Изменяем только те поля, которые были переданы
            //Почта
            if (newUser.getEmail() != null) {
                if (newUser.getEmail().isBlank() || newUser.getEmail().indexOf('@') == -1) {
                    log.warn("Попытка изменения электронной почты пользователя на неправильное");
                    throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
                } else {
                    oldUser.setEmail(newUser.getEmail());
                }
            }
            //Логин
            if (newUser.getLogin() != null) {
                if (newUser.getLogin().isBlank() || newUser.getLogin().indexOf(' ') >= 0) {
                    log.warn("Попытка изменения логина пользователя на неправильное");
                    throw new ValidationException("Логин не может быть пустым и содержать пробелы");
                } else {
                    oldUser.setLogin(newUser.getLogin());
                }
            }
            //Имя
            if (newUser.getName() != null) {
                if (newUser.getName().isBlank()) {
                    if (newUser.getLogin() != null) {
                        oldUser.setName(newUser.getLogin());
                    } else {
                        oldUser.setName(oldUser.getLogin());
                    }
                } else {
                    oldUser.setName(newUser.getName());
                }
            }
            //День рождения
            if (newUser.getBirthday() != null) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    log.warn("Попытка изменения даты рождения пользователя на дату в будущем");
                    throw new ValidationException("Дата рождения не может быть в будущем");
                } else {
                    oldUser.setBirthday(newUser.getBirthday());
                }
            }
            return oldUser;
        }
        log.warn("Попытка изменить пользователя, id которого не существует в базе");
        throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
    }

    //Получаем список пользователей
    @GetMapping
    public Collection<User> findAll() {
        log.info("Получаем список пользователей");
        return new HashSet<>(users.values());
    }

    //Получаем айдишник
    private long getNextId() {
        log.info("Устанавливаем id пользователя");
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
