package aplication.controller;

import aplication.model.Film;
import aplication.service.FilmService;
import aplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ResponseEntity<List<Film>> getMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        var films = filmService.getPopular(count);
        return ResponseEntity.status(HttpStatus.OK).body(films);
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

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable long filmId) {
        filmService.delete(filmId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable long id, @Valid @RequestBody Film film) {
        var updatedFilm = filmService.update(film);
        return ResponseEntity.status(HttpStatus.OK).body(updatedFilm);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(required = false, defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable long id, @PathVariable long userId) {
        if (userService.getById(userId) != null){
            if (filmService.getById(id) != null) {
                filmService.addLike(id, userId);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        }else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> removeLike(@PathVariable long id, @PathVariable long userId) {
        if (userService.getById(userId) != null){
            if (filmService.getById(id) != null) {
                filmService.removeLike(id, userId);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        }else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }
}