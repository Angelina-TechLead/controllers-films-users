package aplication.storage;

import aplication.model.Film;

import java.util.List;

public interface FilmStorage {
    Film create(Film film);
    Film update(Film film);
    void delete(long id);

    void addLike(long filmId, long userId);
    void removeLike(long filmId, long userId);

    List<Film> getAll();
    List<Film> getPopular(int count);
    Film getById(long id);
    void existsById(Long id);
    List<Film> getFilmsByDirector(int directorId, String sortBy);
}

