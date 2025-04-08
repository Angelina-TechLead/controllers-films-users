package aplication.storage;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import org.springframework.stereotype.Component;
import aplication.model.Film;

import java.util.ArrayList;
import java.util.List;


@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final List<Film> films = new ArrayList<>();
    private int currentId = 1;

    public Film add(Film film) {
        film.setId(currentId++);
        films.add(film);
        return film;
    }

    public void delete(long id) {
        films.removeIf(film -> film.getId() == id);
    }

    public Film update(Film film) {
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);
                return film;
            }
        }
        throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
    }

    public List<Film> getAll() {
        return films;
    }

    public Film getById(long id) {
        return films.stream()
                .filter(film -> film.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }
}

