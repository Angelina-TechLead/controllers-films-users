package aplication.service;

import aplication.model.Film;
import aplication.storage.FilmStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void delete(long filmId) {
        filmStorage.delete(filmId);
    }

    public Film addLike(long filmId, long userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(long filmId, long userId) {
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopular(Integer count) {
        int resultCount = (count == null || count <= 0) ? 10 : count;
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(resultCount)
                .toList();
    }

    public Film getById(long filmId) {
        return filmStorage.getById(filmId);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }
}