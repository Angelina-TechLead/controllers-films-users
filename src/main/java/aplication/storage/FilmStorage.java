package aplication.storage;

import aplication.model.Film;

import java.util.List;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(long id);

    List<Film> getAll();

    Film getById(long id);
}

