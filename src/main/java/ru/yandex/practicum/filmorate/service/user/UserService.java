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

    public User create(User newUser) {
        return userStorage.create(newUser);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

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
        if (friendId == null) {
            throw new ValidationException("id друга должен быть указан");
        } else {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("id друга не найден");
            }
        }
        User user = users.get(userId);
        user.getFriends().add(friendId);
        userStorage.update(user);
        User friend = users.get(friendId);
        friend.getFriends().add(userId);
        userStorage.update(friend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        Map<Long, User> users = new HashMap<>(convertToMap(userStorage.findAll()));
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            if (!users.containsKey(userId)) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        if (friendId == null) {
            throw new ValidationException("id друга должен быть указан");
        } else {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("id друга не найден");
            }
        }
        User user = users.get(userId);
        user.getFriends().remove(friendId);
        userStorage.update(user);
        User friend = users.get(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(friend);
    }

    public Collection<User> findUserFriends(Long userId) {
        Optional<User> opUser = userStorage.findOne(userId);
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        }
        if (opUser.isEmpty()) {
            throw new NotFoundException("id пользователя не найден");
        }
        User user = opUser.get();
        return user.getFriends().stream().map(friend -> userStorage.findOne(friend).orElse(new User())).collect(Collectors.toSet());
    }

    public Collection<User> findMutualFriends(Long userId, Long anotherUserId) {
        Optional<User> opUser = userStorage.findOne(userId);
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        }
        if (opUser.isEmpty()) {
            throw new NotFoundException("id пользователя не найден");
        }
        Optional<User> opAnotherUser = userStorage.findOne(anotherUserId);
        if (anotherUserId == null) {
            throw new ValidationException("id друга должен быть указан");
        }
        if (opAnotherUser.isEmpty()) {
            throw new NotFoundException("id друга не найден");
        }
        User user = opUser.get();
        User anotherUser = opAnotherUser.get();
        HashSet<Long> userFriendsIds = new HashSet<>(user.getFriends());
        HashSet<Long> anotherUserFriendsIds = new HashSet<>(anotherUser.getFriends());
        HashSet<Long> mutualFriendsIds = new HashSet<>(userFriendsIds);
        mutualFriendsIds.retainAll(anotherUserFriendsIds);
        return mutualFriendsIds.stream().map(mutualFriend -> userStorage.findOne(mutualFriend).orElse(new User())).collect(Collectors.toSet());
    }

    private Map<Long, User> convertToMap(Collection<User> users) {
        return users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }
}
