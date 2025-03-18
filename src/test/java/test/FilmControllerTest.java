package test;

import aplication.controller.FilmController;
import aplication.exception.ValidationException;
import aplication.model.Film;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class FilmControllerTest {

    private final FilmController filmController = new FilmController();

    @Test
    public void testValidateFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testValidateFilmWithValidData() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A great movie");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    public void testValidateFilmWithTooLongDescription() {
        Film film = new Film();
        film.setName("Some name");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testValidateFilmWithInvalidReleaseDate() {
        Film film = new Film();
        film.setName("Some name");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }
}
