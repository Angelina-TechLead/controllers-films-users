package aplication.storage;

import aplication.model.Film;

import java.util.List;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(long id);

    Film addLike(long filmId, long userId);

    Film removeLike(long filmId, long userId);

    List<Film> getAll();

    Film getById(long id);
}

