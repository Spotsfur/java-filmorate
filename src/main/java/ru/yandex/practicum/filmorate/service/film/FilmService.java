package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film create(Film newFilm) {
        log.info("Создание фильма: {}", newFilm.getName());
        validateFilm(newFilm);
        return filmStorage.create(newFilm);
    }

    public Film update(Film newFilm) {
        log.info("Обновление фильма с id: {}", newFilm.getId());
        if (newFilm.getId() == null) {
            throw new ValidationException("id фильма должен быть указан");
        }
        Film oldFilm = filmStorage.findOne(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + newFilm.getId() + " не найден"));

        Film nullReplacedFilm = replaceNullDataFilm(newFilm, oldFilm);
        validateFilm(nullReplacedFilm);
        return filmStorage.update(nullReplacedFilm);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findOne(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Запрос на добавление лайка фильму {} от пользователя {}", filmId, userId);
        findById(filmId);
        checkUserExistsById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка у фильма {} от пользователя {}", filmId, userId);
        findById(filmId);
        checkUserExistsById(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> topFilms(int count) {
        log.info("Запрос на получение топ {} фильмов", count);
        return filmStorage.getPopular(count);
    }

    private void validateFilm(Film film) {
        //Название не может быть пустым
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        //Максимальная длина описания — 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        //Дата релиза — не раньше 28 декабря 1895 года
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        //Продолжительность фильма должна быть положительным числом
        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма отрицательная или равна нулю");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        //Валидация MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.findById(film.getMpa().getId());
        }

        //Валидация жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }
    }

    //Эта функция теперь переписывает нулль поля фильма при обновлении из старых данных
    private Film replaceNullDataFilm(Film newFilm, Film oldFilm) {
        if (newFilm.getName() == null) {
            newFilm.setName(oldFilm.getName());
        }
        if (newFilm.getDescription() == null) {
            newFilm.setDescription(oldFilm.getDescription());
        }
        if (newFilm.getReleaseDate() == null) {
            newFilm.setReleaseDate(oldFilm.getReleaseDate());
        }
        if (newFilm.getMpa() == null) {
            newFilm.setMpa(oldFilm.getMpa());
        }
        return newFilm;
    }

    private void checkUserExistsById(Long userId) {
        userStorage.findOne(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден при работе с лайками", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
    }
}
