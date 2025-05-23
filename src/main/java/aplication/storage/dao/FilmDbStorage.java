package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.Film;
import aplication.storage.FilmStorage;
import aplication.storage.dao.mappers.FilmRowMapper;
import aplication.storage.dao.queries.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Component
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(FilmQueryConstants.CREATE_FILM_QUERY, new String[]{"id"});
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
            jdbc.batchUpdate(FilmQueryConstants.ADD_FILM_GENRE_QUERY, film.getGenres(), film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genre.getId());
                    });
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Жанры не найдены");
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            try {
                jdbc.batchUpdate(FilmQueryConstants.ADD_FILM_DIRECTOR_QUERY,
                        film.getDirectors().stream()
                                .map(director -> new Object[]{film.getId(), director.getId()})
                                .collect(Collectors.toList()));
            } catch (DataIntegrityViolationException e) {
                throw new NotFoundException("Такой режиссер не найден");
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        var updateDuration = film.getDuration();
        var updateRating = film.getMpa().getId();

        try {
            jdbc.update(FilmQueryConstants.UPDATE_FILM_QUERY,
                    film.getName(), film.getDescription(), film.getReleaseDate(), updateDuration, updateRating,
                    film.getId());
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Не удалось обновить фильм");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не удалось обновить фильм");
        }

        try {
            jdbc.update(FilmQueryConstants.REMOVE_FILM_GENRES_QUERY, film.getId());
            jdbc.batchUpdate(FilmQueryConstants.ADD_FILM_GENRE_QUERY, film.getGenres(), film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genre.getId());
                    });
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не обновить жанры");
        }

        jdbc.update(FilmQueryConstants.REMOVE_FILM_DIRECTOR_QUERY, film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            try {
                jdbc.batchUpdate(FilmQueryConstants.ADD_FILM_DIRECTOR_QUERY,
                        film.getDirectors().stream()
                                .map(director -> new Object[]{film.getId(), director.getId()})
                                .collect(Collectors.toList()));
            } catch (DataIntegrityViolationException e) {
                throw new NotFoundException("Такой режиссер не найден");
            }
        }
        return getById(film.getId());
    }

    @Override
    public void delete(long id) {
        jdbc.update(FilmQueryConstants.DELETE_FILM_BY_ID_QUERY, id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update(LikeQueryConstants.ADD_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update(LikeQueryConstants.DELETE_QUERY, filmId, userId);
    }

    @Override
    public Film getById(long id) {
        try {
            return jdbc.queryForObject(FilmQueryConstants.GET_FILM_BY_ID_QUERY, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public void existsById(Long id) {
        try {
            jdbc.queryForObject(FilmQueryConstants.GET_SIMPLE_FILM_QUERY, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        return jdbc.query(FilmQueryConstants.GET_FILMS_QUERY, new FilmRowMapper());
    }

    @Override
    public List<Film> findByFilters(Map<String, Object> filters) {
        FilmQueryBuilder queryBuilder = new FilmQueryBuilder().addFilters(filters);

        var sql = queryBuilder.buildQuery();
        var params = queryBuilder.getParameters();

        return namedParameterJdbcTemplate.query(sql, params, new FilmRowMapper());
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        String query = switch (sortBy.toLowerCase()) {
            case "likes" -> FilmQueryConstants.GET_DIRECTOR_FILMS_SORTED_BY_LIKES;
            case "year" -> FilmQueryConstants.GET_DIRECTOR_FILMS_SORTED_BY_YEAR;
            default -> throw new ValidationException("Параметр sortBy может быть только 'year' или 'likes'");
        };

        return jdbc.query(query, new FilmRowMapper(), directorId);
    }

    public void addDirector(long filmId, int directorId) {
        jdbc.update(FilmQueryConstants.ADD_FILM_DIRECTOR_QUERY, filmId, directorId);
    }

    public void removeDirectors(long filmId) {
        jdbc.update(FilmQueryConstants.REMOVE_FILM_DIRECTOR_QUERY, filmId);
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        return jdbc.query(FilmQueryConstants.GET_COMMON_FILMS_QUERY, new FilmRowMapper(), userId, friendId);
    }
}
