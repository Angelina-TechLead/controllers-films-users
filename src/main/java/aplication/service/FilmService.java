package aplication.service;

import aplication.exception.NotFoundException;
import aplication.model.Film;
import aplication.storage.FilmStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film add(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (getById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        return filmStorage.update(film);
    }

    public void delete(long filmId) {
        filmStorage.delete(filmId);
    }

    public void addLike(long filmId, long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public Film getById(long filmId) {
        return filmStorage.getById(filmId);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public List<Film> search(Map<String, Object> filters) {
        return filmStorage.findByFilters(filters);
    }
}