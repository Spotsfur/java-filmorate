package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaStorage;

    public Collection<Mpa> findAll() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaStorage.findAll();
    }

    public Mpa findById(Integer id) {
        log.info("Получен запрос на получение рейтинга MPA с id {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }
}
