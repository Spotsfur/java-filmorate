package ru.yandex.practicum.filmorate.service.film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FilmService {

    //В методах мы получаем данные из Hashmap пользователей, потому UserStorage нам тоже нужен
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Comparator<Film> filmLikeComparator = Comparator.comparingLong((Film film) -> film.getLikes().size()).reversed();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    //Добавляем фильм
    public Film create(Film newFilm) {
        return filmStorage.create(newFilm);
    }

    //Изменяем фильм
    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    //Получаем список фильмов
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    //Добавляем лайк фильму
    public void addLike(Long filmId, Long userId) {
        Map<Long, Film> films = new HashMap<>(convertFilmsToMap(filmStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap фильм с переданным userId
        if (filmId == null) {
            throw new ValidationException("id фильма должен быть указан");
        } else {
            if (!films.containsKey(filmId)) {
                throw new NotFoundException("id фильма не найден");
            }
        }
        //Проверяем, существует ли пользователь с переданным userId
        //Не будем работать с данными пользователя, проверки существования id достаточно
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            Collection<User> users = userStorage.findAll();
            boolean userIsExist = users.stream()
                    .anyMatch(user -> user.getId().equals(userId));
            if (!userIsExist) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Работаем с данными фильма
        Film film = films.get(filmId);
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    //Убираем лайк фильму
    public void removeLike(Long filmId, Long userId) {
        Map<Long, Film> films = new HashMap<>(convertFilmsToMap(filmStorage.findAll()));
        //Проверяем, существует ли в полученном hashmap фильм с переданным filmId
        if (filmId == null) {
            throw new ValidationException("id фильма должен быть указан");
        } else {
            if (!films.containsKey(filmId)) {
                throw new NotFoundException("id фильма не найден");
            }
        }
        //Проверяем, существует ли пользователь с переданным userId
        //Не будем работать с данными пользователя, проверки существования id достаточно
        if (userId == null) {
            throw new ValidationException("id пользователя должен быть указан");
        } else {
            Collection<User> users = userStorage.findAll();
            boolean userIsExist = users.stream()
                    .anyMatch(user -> user.getId().equals(userId));
            if (!userIsExist) {
                throw new NotFoundException("id пользователя не найден");
            }
        }
        //Работаем с данными фильма
        Film film = films.get(filmId);
        film.getLikes().remove(filmId);
        filmStorage.update(film);
    }

    //Получаем топ фильмов
    public Collection<Film> topFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество должно быть больше нуля");
        }
        return filmStorage.findAll().stream()
                .sorted(filmLikeComparator)
                .limit(count)
                .toList();
    }

    //Вспомогательный метод конвертации hashset фильмов в map для удобства использования
    private Map<Long, Film> convertFilmsToMap(Collection<Film> films) {
        return films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
    }
}
