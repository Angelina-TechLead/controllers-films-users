package aplication.storage;

import aplication.exception.NotFoundException;
import aplication.model.Film;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final List<Film> films = new ArrayList<>();
    private int currentId = 1;

    public Film create(Film film) {
        film.setId(currentId++);
        films.add(film);
        return film;
    }

    public void delete(long id) {
        films.removeIf(film -> film.getId() == id);
    }

    @Override
    public Film addLike(long filmId, long userId) {
        var film = getById(filmId);
        film.getLikes().add(userId);

        update(film);

        return film;
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        var film = getById(filmId);
        film.getLikes().remove(userId);

        update(film);
        return film;
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

    public List<Film> getPopular(int count) {
        int resultCount = (count <= 0) ? 10 : count;
        return getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(resultCount)
                .toList();
    }

    public Film getById(long id) {
        return films.stream()
                .filter(film -> film.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    @Override
    public void existsById(Long id) {

    }
}

