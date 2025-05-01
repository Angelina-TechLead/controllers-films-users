package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.Film;
import aplication.model.Genre;
import aplication.storage.FilmStorage;
import aplication.storage.dao.mappers.FilmRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Primary
@Component
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(FilmRowMapper.CREATE_FILM_QUERY, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());

                if (film.getReleaseDate() != null) {
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                } else {
                    ps.setNull(3, Types.DATE);
                }

                if (film.getDuration() > 0) {
                    ps.setLong(4, (long) film.getDuration());
                } else {
                    ps.setNull(4, Types.BIGINT);
                }

                if (film.getMpa() != null) {
                    ps.setLong(5, film.getMpa().getId());
                } else {
                    ps.setNull(5, Types.BIGINT);
                }

                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Не удалось создать фильм");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не удалось создать фильм");
        }
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        try {
            for (Genre genre : film.getGenres()) {
                jdbc.update(FilmRowMapper.ADD_FILM_GENRE_QUERY, film.getId(), genre.getId());
            }
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Жанры не найдены");
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        var updateDuration = film.getDuration();
        var updateRating = film.getMpa().getId();

        try {
            jdbc.update(FilmRowMapper.UPDATE_FILM_QUERY,
                    film.getName(), film.getDescription(), film.getReleaseDate(), updateDuration, updateRating,
                    film.getId());
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Не удалось обновить фильм");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не удалось обновить фильм");
        }
        try {
            jdbc.update(FilmRowMapper.REMOVE_FILM_GENRES_QUERY, film.getId());
            for (Genre genre : film.getGenres()) {
                jdbc.update(FilmRowMapper.ADD_FILM_GENRE_QUERY, film.getId(), genre.getId());
            }
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не обновить жанры");
        }
        return film;
    }

    @Override
    public void delete(long id) {
        jdbc.update(FilmRowMapper.DELETE_FILM_BY_ID_QUERY, id);
    }

    @Override
    public Film addLike(long filmId, long userId) {
        // SQL-запрос для добавления лайка
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        // Выполняем вставку лайка
        jdbc.update(sql, filmId, userId);

        // Возвращаем обновлённый объект фильма
        return getById(filmId); // Метод getById загружает фильм из базы данных вместе с лайками и жанрами
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        // SQL-запрос для удаления лайка
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        // Выполняем удаление лайка
        jdbc.update(sql, filmId, userId);

        // Возвращаем обновлённый объект фильма
        return getById(filmId); // Метод getById загружает фильм из базы данных вместе с лайками и жанрами
    }

    @Override
    public Film getById(long id) {
        try {
            return jdbc.queryForObject(FilmRowMapper.GET_FILM_BY_ID_QUERY, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public void existsById(Long id) {
        try {
            jdbc.queryForObject(FilmRowMapper.GET_SIMPLE_FILM_QUERY, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        return jdbc.query(FilmRowMapper.GET_FILMS_QUERY, new FilmRowMapper());
    }

    @Override
    public List<Film> getPopular(int count) {
        var resultCount = (count <= 0) ? 10 : count;
        return jdbc.query(FilmRowMapper.GET_POPULAR_FILMS_QUERY, new FilmRowMapper(), resultCount);
    }

    private final JdbcTemplate jdbc;

    // Приватный метод для добавления жанров
    private void addGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(sql, film.getId(), genre.getId());
            }
        }
    }

    // Приватный метод для обновления жанров
    private void updateGenres(Film film) {
        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());

        // Добавляем новые жанры
        addGenres(film);
    }
}