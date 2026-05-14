package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    public Film create(Film newFilm) {
        log.info("Добавляем новый фильм");
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
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    public Film update(Film newFilm) {
        log.info("Изменяем фильм");
        if (newFilm.getId() == null) {
            log.warn("Попытка изменения фильма с неправильным id");
            throw new ValidationException("id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Попытка изменить фильм, id которого не существует в базе");
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }
        Film oldFilm = films.get(newFilm.getId());
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Попытка изменения названия фильма на пустое");
                throw new ValidationException("Название не может быть пустым");
            } else {
                oldFilm.setName(newFilm.getName());
            }
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.warn("Попытка изменения длины описания фильма на слишком большую");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            } else {
                oldFilm.setDescription(newFilm.getDescription());
            }
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
                log.warn("Попытка изменить дату выхода фильма на ранее чем 28 декабря 1895 года");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            } else {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
        }
        if (newFilm.getDuration() <= 0) {
            log.warn("Попытка изменить продолжительность фильма на на значение, не являющиеся положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        } else {
            oldFilm.setDuration(newFilm.getDuration());
        }
        return oldFilm;
    }

    @Override
    public Optional<Film> findOne(Long id) {
        log.info("Получение фильма по id из памяти: {}", id);
        return Optional.ofNullable(films.get(id));
    }

    public Collection<Film> findAll() {
        log.info("Получаем список фильмов");
        return new HashSet<>(films.values());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка в памяти для фильма {} от пользователя {}", filmId, userId);
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        film.getLikes().add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        log.info("Удаление лайка в памяти для фильма {} от пользователя {}", filmId, userId);
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        log.info("Получение топ {} популярных фильмов из памяти", count);
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(java.util.stream.Collectors.toList());
    }

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
