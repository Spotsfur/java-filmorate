package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbc;

    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbc.query(sql, this::mapRowToGenre);
    }

    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        return jdbc.query(sql, this::mapRowToGenre, id).stream().findFirst();
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
