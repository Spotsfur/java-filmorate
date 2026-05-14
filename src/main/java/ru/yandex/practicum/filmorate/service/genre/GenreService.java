package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;

    public Collection<Genre> findAll() {
        log.info("Получен запрос на получение всех жанров");
        return genreStorage.findAll();
    }

    public Genre findById(Integer id) {
        log.info("Получен запрос на получение жанра с id {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
