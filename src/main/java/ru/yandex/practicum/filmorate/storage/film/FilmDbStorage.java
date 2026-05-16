package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Film create(Film newFilm) {
        String sql = """
                INSERT INTO films (name, description, releaseDate, duration, rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, newFilm.getName());
            ps.setString(2, newFilm.getDescription());
            ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
            ps.setInt(4, newFilm.getDuration());
            ps.setInt(5, newFilm.getMpa().getId());
            return ps;
        }, keyHolder);

        newFilm.setId(keyHolder.getKey().longValue());
        updateGenres(newFilm);
        return newFilm;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = """
                UPDATE films SET name = ?, description = ?, releaseDate = ?,
                duration = ?, rating_id = ? WHERE id = ?
                """;

        int rowsUpdated = jdbc.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }
        updateGenres(newFilm);
        return newFilm;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.rating_id,
                r.name AS mpa_name
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                ORDER BY f.id
                """;

        Collection<Film> films = jdbc.query(sql, this::mapRowToFilm);
        loadGenres(films);
        return films;
    }

    @Override
    public Optional<Film> findOne(Long id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.rating_id,
                r.name AS mpa_name
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                WHERE f.id = ?
                """;

        Collection<Film> films = jdbc.query(sql, this::mapRowToFilm, id);
        loadGenres(films);
        return films.stream().findFirst();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь с id {} ставит лайк фильму с id {}", userId, filmId);
        String sql = "INSERT INTO favorites (user_id, film_id) VALUES (?, ?)";
        jdbc.update(sql, userId, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        log.info("Пользователь с id {} удаляет лайк у фильма с id {}", userId, filmId);
        String sql = "DELETE FROM favorites WHERE user_id = ? AND film_id = ?";
        int rowsDeleted = jdbc.update(sql, userId, filmId);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Лайк от пользователя " + userId + " фильму " + filmId + " не найден");
        }
    }

    @Override
    public Collection<Film> getPopular(int count) {
        log.info("Получение топ {} популярных фильмов", count);

        String sql = """
                SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.rating_id,
                r.name AS mpa_name
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                LEFT JOIN (SELECT film_id, COUNT(user_id) AS cnt FROM favorites GROUP BY film_id) fav
                ON f.id = fav.film_id
                ORDER BY COALESCE(fav.cnt, 0) DESC, f.id ASC
                LIMIT ?
                """;

        Collection<Film> popularFilms = jdbc.query(sql, this::mapRowToFilm, count);
        loadGenres(popularFilms);
        return popularFilms;
    }

    private void updateGenres(Film film) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                });
    }

    private void loadGenres(Collection<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        List<String> idStrings = films.stream()
                .map(film -> String.valueOf(film.getId()))
                .toList();

        String sql = """
            SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id IN (%s)
            """.formatted(String.join(",", idStrings));

        jdbc.query(sql, (rs) -> {
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = Genre.builder()
                        .id(rs.getInt("genre_id"))
                        .name(rs.getString("genre_name"))
                        .build();

                films.stream()
                        .filter(f -> f.getId().equals(filmId))
                        .findFirst()
                        .ifPresent(f -> {
                            if (f.getGenres() == null) {
                                f.setGenres(new LinkedHashSet<>());
                            }
                            f.getGenres().add(genre);
                        });
            }
            return null;
        });
    }


    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(Mpa.builder()
                        .id(rs.getInt("rating_id"))
                        .name(rs.getString("mpa_name"))
                        .build())
                .build();
    }
}
