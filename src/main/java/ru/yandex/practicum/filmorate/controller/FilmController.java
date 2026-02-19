package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate FIRST_FILM_DATE = LocalDate.ofYearDay(1895, 362);
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    //Добавляем фильм
    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        log.info("Добавляем новый фильм");
        //Проверки
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.warn("Попытка добавления фильма с неправильным названием");
            throw new ValidationException("Название не может быть пустым");
        }
        if (newFilm.getDescription().length() > 200) {
            log.warn("Попытка добавления фильма с большой длиной описания");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (newFilm.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.warn("Попытка добавить фильм с датой выхода ранее 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (newFilm.getDuration() <= 0) {
            log.warn("Попытка добавить фильм с продолжительностью, не являющейся положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        //Суём данные
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    //Изменяем фильм
    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Изменяем фильм");
        if (newFilm.getId() == null) {
            log.warn("Попытка изменения фильма с неправильным id");
            throw new ValidationException("id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            //Изменяем только те поля, которые были переданы
            //Название
            if (newFilm.getName() != null) {
                if (newFilm.getName().isBlank()) {
                    log.warn("Попытка изменения названия фильма на пустое");
                    throw new ValidationException("Название не может быть пустым");
                } else {
                    oldFilm.setName(newFilm.getName());
                }
            }
            //Описание
            if (newFilm.getDescription() != null) {
                if (newFilm.getDescription().length() > 200) {
                    log.warn("Попытка изменения длины описания фильма на слишком большую");
                    throw new ValidationException("Максимальная длина описания — 200 символов");
                } else {
                    oldFilm.setDescription(newFilm.getDescription());
                }
            }
            //Дата
            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
                    log.warn("Попытка изменить дату выхода фильма на ранее чем 28 декабря 1895 года");
                    throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
                } else {
                    oldFilm.setReleaseDate(newFilm.getReleaseDate());
                }
            }
            //Продолжительность
            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getDuration() <= 0) {
                    log.warn("Попытка изменить продолжительность фильма на на значение, не являющиеся положительным числом");
                    throw new ValidationException("Продолжительность фильма должна быть положительным числом");
                } else {
                    oldFilm.setDuration(newFilm.getDuration());
                }
            }
            return oldFilm;
        }
        log.warn("Попытка изменить фильм, id которого не существует в базе");
        throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
    }

    //Получаем список фильмов
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получаем список фильмов");
        return films.values();
    }

    //Получаем айдишник
    private long getNextId() {
        log.info("Устанавливаем id фильма");
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
