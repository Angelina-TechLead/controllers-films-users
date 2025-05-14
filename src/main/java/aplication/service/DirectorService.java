package aplication.service;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.Director;
import aplication.model.Film;
import aplication.storage.dao.DirectorDbStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public List<Director> getAll() {
        return directorDbStorage.getAll();
    }

    public Director getById(int id) {
        return directorDbStorage.getById(id);
    }

    public Director create(Director director) {
        validateDirector(director);
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        validateDirector(director);
        return directorDbStorage.update(director);
    }

    public void delete(int id) {
        directorDbStorage.delete(id);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        List<Film> films = directorDbStorage.getFilmsByDirector(directorId);

        if ("year".equalsIgnoreCase(sortBy)) {
            films.sort((f1, f2) -> f2.getReleaseDate().compareTo(f1.getReleaseDate()));
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            films.sort((f1, f2) -> Integer.compare(
                    f2.getLikes().size(),
                    f1.getLikes().size()
            ));
        }

        return films;
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Director name cannot be empty");
        }
    }
}