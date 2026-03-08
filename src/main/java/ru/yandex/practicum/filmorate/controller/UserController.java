package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Создаём пользователя
    @PostMapping
    public User create(@RequestBody User newUser) {
        return userService.create(newUser);
    }

    //Обновляем пользователя
    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
    }

    //Получаем список пользователей
    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    //PUT /users/{id}/friends/{friendId}
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    //DELETE /users/{id}/friends/{friendId}
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    //GET /users/{id}/friends
    @GetMapping("/{id}/friends")
    public Collection<User> findUserFriends(@PathVariable Long id) {
        return userService.findUserFriends(id);
    }

    //GET /users/{id}/friends/common/{otherId}
    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findMutualFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.findMutualFriends(id, otherId);
    }
}
