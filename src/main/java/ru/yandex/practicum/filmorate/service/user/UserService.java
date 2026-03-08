package ru.yandex.practicum.filmorate.service.user;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage storage) {
        this.userStorage = storage;
    }

    //Создаём пользователя
    public User create(User newUser) {
        return userStorage.create(newUser);
    }

    //Обновляем пользователя
    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    //Получаем список пользователей
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    //Добавляем друга
    public void addFriend(Long userId, Long friendId) {
        Map<Long, User> users = new HashMap<>(convertToMap(userStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap пользователь с переданным userId
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            if (!users.containsKey(userId)) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Проверяем, существует ли в полученном hashmap пользователь с переданным friendId
        if (friendId == null) {
            throw new ValidationException("id друга должен быть указан");
        } else {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("id друга не найден");
            }
        }
        //Обновляем пользователя
        User user = users.get(userId);
        user.getFriends().add(friendId);
        userStorage.update(user);
        //Обновляем друга пользователя
        User friend = users.get(friendId);
        friend.getFriends().add(userId);
        userStorage.update(friend);
    }

    //Удаляем друга
    public void deleteFriend(Long userId, Long friendId) {
        Map<Long, User> users = new HashMap<>(convertToMap(userStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap пользователь с переданным userId
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            if (!users.containsKey(userId)) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Проверяем, существует ли в полученном hashmap пользователь с переданным friendId
        if (friendId == null) {
            throw new ValidationException("id друга должен быть указан");
        } else {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("id друга не найден");
            }
        }
        //Обновляем пользователя
        User user = users.get(userId);
        user.getFriends().remove(friendId);
        userStorage.update(user);
        //Обновляем друга пользователя
        User friend = users.get(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(friend);
    }

    //Получаем список пользователей, являющихся пользователю друзьями
    public Collection<User> findUserFriends(Long userId) {
        Map<Long, User> users = new HashMap<>(convertToMap(userStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap пользователь с переданным userId
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            if (!users.containsKey(userId)) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Не получается это красиво в стрим оформить, сделал цикл
        User user = users.get(userId);
        Collection<User> friends = new HashSet<>();
        for (Long friend : user.getFriends()) {
            friends.add(users.get(friend));
        }
        return friends;
    }

    //Получаем список пользователей, являющихся общими друзьями двух пользователей
    public Collection<User> findMutualFriends(Long userId, Long anotherUserId) {
        Map<Long, User> users = new HashMap<>(convertToMap(userStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap пользователь с переданным userId
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            if (!users.containsKey(userId)) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Проверяем, существует ли в полученном hashmap пользователь с переданным anotherUserId
        if (anotherUserId == null) {
            throw new ValidationException("id друга должен быть указан");
        } else {
            if (!users.containsKey(anotherUserId)) {
                throw new NotFoundException("id друга не найден");
            }
        }
        //Извлекаем пользователей
        User user = users.get(userId);
        User anotherUser = users.get(anotherUserId);
        //Извлекаем списки их друзей
        HashSet<Long> userFriendsIds = new HashSet<>(user.getFriends());
        HashSet<Long> anotherUserFriendsIds = new HashSet<>(anotherUser.getFriends());
        //Делаем общий список по id
        HashSet<Long> mutualFriendsIds = new HashSet<>(userFriendsIds);
        mutualFriendsIds.retainAll(anotherUserFriendsIds);
        //Формируем список пользователей
        Collection<User> mutualFriends = new HashSet<>();
        for (Long friend : mutualFriendsIds) {
            mutualFriends.add(users.get(friend));
        }
        return mutualFriends;
    }

    //Вспомогательный метод конвертации hashset пользователей в map для удобства использования
    private Map<Long, User> convertToMap(Collection<User> users) {
        return users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }
}
