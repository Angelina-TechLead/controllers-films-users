package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.model.Director;
import aplication.model.Film;
import aplication.storage.dao.mappers.DirectorRowMapper;
import aplication.storage.dao.mappers.FilmRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Director> getAll() {
        return jdbcTemplate.query(DirectorRowMapper.GET_ALL_QUERY, new DirectorRowMapper());
    }

    public Director getById(int id) {
        try {
            return jdbcTemplate.queryForObject(
                    DirectorRowMapper.GET_BY_ID_QUERY,
                    new DirectorRowMapper(),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Director with id " + id + " not found");
        }
    }

    public Director create(Director director) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(
                    DirectorRowMapper.CREATE_QUERY,
                    new String[]{"id"}
            );
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    public Director update(Director director) {
        int updated = jdbcTemplate.update(
                DirectorRowMapper.UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
        if (updated == 0) {
            throw new NotFoundException("Director not found");
        }
        return director;
    }

    public void delete(int id) {
        int deleted = jdbcTemplate.update(DirectorRowMapper.DELETE_QUERY, id);
        if (deleted == 0) {
            throw new NotFoundException("Director not found");
        }
    }

    public List<Film> getFilmsByDirector(int directorId) {
        return jdbcTemplate.query(
                DirectorRowMapper.GET_FILMS_BY_DIRECTOR,
                new FilmRowMapper(),
                directorId
        );
    }
}
