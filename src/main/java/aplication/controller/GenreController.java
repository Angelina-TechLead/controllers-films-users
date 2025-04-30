package aplication.controller;

import aplication.exception.NotFoundException;
import aplication.model.Genre;
import aplication.storage.dao.GenreDbStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreDbStorage genreStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        var data = genreStorage.getAll();
        if (data.isEmpty()) {
            throw new NotFoundException("Рейтинги не найдены");
        } else {
            return data;
        }
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        var data = genreStorage.getById(id);
        if (data == null) {
            throw new NotFoundException("Жанр не найдены");
        } else {
            return data;
        }
    }
}
