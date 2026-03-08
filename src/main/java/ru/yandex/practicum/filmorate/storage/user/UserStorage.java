package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    public User create(User newUser);

    public User update(User newUser);

    public Collection<User> findAll();
}
