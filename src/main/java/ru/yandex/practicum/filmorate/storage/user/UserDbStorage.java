package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private static final Logger log = LoggerFactory.getLogger(UserDbStorage.class);

    @Override
    public User create(User newUser) {
        log.info("Добавляем нового пользователя");
        //Отсюда валятся эксепшны
        createValidation(newUser);
        if (newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, new String[]{"id"});
            ps.setString(1, newUser.getEmail());
            ps.setString(2, newUser.getLogin());
            ps.setString(3, newUser.getName());
            ps.setDate(4, Date.valueOf(newUser.getBirthday()));
            return ps;
        }, keyHolder);
        newUser.setId(keyHolder.getKey().longValue());
        return newUser;
    }

    @Override
    public User update(User newUser) {
        log.info("Изменяем пользователя");
        //Отсюда валятся эксепшны
        User oldUser = updateValidation(newUser);

        if (newUser.getEmail() == null) newUser.setEmail(oldUser.getEmail());
        if (newUser.getBirthday() == null) newUser.setBirthday(oldUser.getBirthday());
        if (newUser.getLogin() == null) newUser.setLogin(oldUser.getLogin());
        if (newUser.getName() == null) {
            newUser.setName(oldUser.getName());
        } else if (newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbc.update(sql, newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(), newUser.getId());
        return newUser;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        return jdbc.query(sql, this::mapRowToUser);
    }

    @Override
    public Optional<User> findOne(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, this::mapRowToUser, id).stream().findFirst();
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

    @Override
    public void addFriend(Long userId, Long friendId) {
        findOne(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        findOne(friendId).orElseThrow(() -> new NotFoundException("Друг не найден"));

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbc.update(sql, userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sql, userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbc.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.id = f1.friend_id " +
                "JOIN friends f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbc.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User updateValidation(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Попытка изменения пользователя с неправильным id");
            throw new ValidationException("id должен быть указан");
        }
        User oldUser = findOne(newUser.getId())
                .orElseThrow(() -> {
                    log.warn("Попытка изменить пользователя, id которого не существует в базе");
                    return new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
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

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
