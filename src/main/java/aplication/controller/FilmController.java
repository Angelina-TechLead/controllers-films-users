package aplication.controller;

import aplication.model.Film;
import aplication.service.FilmService;
import aplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Film>> getFilms() {
        if (filmService.getAll() != null) {
            return ResponseEntity.status(HttpStatus.OK).body(filmService.getAll());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable long id) {
        var film = filmService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(film);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> popular(@RequestParam Map<String, Object> params) {
        List<Film> films = filmService.search(params);

        return films.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
                : ResponseEntity.ok(films);
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        var newFilm = filmService.add(film);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFilm);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        var updatedUser = filmService.update(film);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable long id, @Valid @RequestBody Film film) {
        var updatedFilm = filmService.update(film);
        return ResponseEntity.status(HttpStatus.OK).body(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable long id, @PathVariable long userId) {
        if (userService.getById(userId) != null) {
            if (filmService.getById(id) != null) {
                filmService.addLike(id, userId);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> removeLike(@PathVariable long id, @PathVariable long userId) {
        if (userService.getById(userId) != null) {
            if (filmService.getById(id) != null) {
                filmService.removeLike(id, userId);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Film>> search(
            @RequestParam(value = "query", required = false) String searchValue,
            @RequestParam(value = "by", required = false) List<String> filterTypes) {

        if (searchValue == null || filterTypes == null || filterTypes.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Map<String, Object> filters = new HashMap<>();

        for (String filter : filterTypes) {
            switch (filter.toLowerCase()) {
                case "title" -> filters.put("title", searchValue);
                case "director" -> filters.put("director", searchValue);
            }
        }

        var films = filmService.search(filters);

        return films.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
                : ResponseEntity.status(HttpStatus.OK).body(films);
    }
}
