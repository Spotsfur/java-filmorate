package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbc;

    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM ratings ORDER BY id";
        return jdbc.query(sql, this::mapRowToMpa);
    }

    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        return jdbc.query(sql, this::mapRowToMpa, id).stream().findFirst();
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
