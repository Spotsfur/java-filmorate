package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film newFilm);

    Film update(Film newFilm);

    Collection<Film> findAll();

    Optional<Film> findOne(Long id);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopular(int count);
}
