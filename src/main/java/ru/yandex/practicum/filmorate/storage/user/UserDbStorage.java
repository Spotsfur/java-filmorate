package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private static final Logger log = LoggerFactory.getLogger(UserDbStorage.class);

    @Override
    public User create(User newUser) {
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

    @Override
    public boolean existsById(Long id) {
        String sql = """
            SELECT COUNT(1)
            FROM users
            WHERE id = ?
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count > 0;
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
        String sql = """
                SELECT u.* FROM users u
                JOIN friends f ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;
        return jdbc.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friends f1 ON u.id = f1.friend_id
                JOIN friends f2 ON u.id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;
        return jdbc.query(sql, this::mapRowToUser, userId, otherId);
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
