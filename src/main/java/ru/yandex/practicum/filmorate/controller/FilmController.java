package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    //Добавляем фильм
    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        return filmService.create(newFilm);
    }

    //Изменяем фильм
    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    //Получаем список фильмов
    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    //PUT /films/{id}/like/{userId}
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId}
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    //GET /films/popular?count={count}
    @GetMapping("/popular")
    public Collection<Film> topFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.topFilms(count);
    }
}
