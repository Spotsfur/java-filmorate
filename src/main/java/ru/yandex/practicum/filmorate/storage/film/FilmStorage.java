package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film create(Film newFilm);

    Film update(Film newFilm);

    Collection<Film> findAll();
}
