package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Film create(Film newFilm);

    public Film update(Film newFilm);

    public Collection<Film> findAll();
}
