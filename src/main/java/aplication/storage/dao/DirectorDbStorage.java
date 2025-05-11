package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.Director;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

public class DirectorDbStorage {
    private final JdbcTemplate jdbc;

    private final RowMapper<Director> directorRowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("director_name"));
        return director;
    };

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public List<Director> getAll() {
        String sql = "SELECT * FROM directors ORDER BY id ASC";
        try {
            return jdbc.query(sql, directorRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссеры не найдены");
        }
    }

    public Director getById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, directorRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден");
        }
    }

    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO directors (director_name) VALUES (?)";

        try {
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, director.getName());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Режиссер с таким именем уже существует");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не удалось создать режиссера");
        }
        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET director_name = ? WHERE id = ?";

        try {
            int updated = jdbc.update(sql,
                    director.getName(),
                    director.getId());

            if (updated == 0) {
                throw new NotFoundException("Режиссер с ID " + director.getId() + " не найден");
            }
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Режиссер с таким именем уже существует");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Не удалось обновить режиссера");
        }
        return director;
    }

    public void delete(int id) {
        String checkFilmsSql = "SELECT COUNT(*) FROM film_directors WHERE director_id = ?";
        int filmCount = jdbc.queryForObject(checkFilmsSql, Integer.class, id);

        if (filmCount > 0) {
            throw new ValidationException(
                    "Невозможно удалить режиссера - существуют связанные фильмы. " +
                            "Сначала удалите связи с фильмами.");
        }

        String deleteSql = "DELETE FROM directors WHERE id = ?";
        int deleted = jdbc.update(deleteSql, id);

        if (deleted == 0) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден");
        }
    }
}