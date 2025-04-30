package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.model.Genre;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Primary
@Component
@Repository
public class GenreDbStorage {
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAll() {
        String sql = "SELECT * FROM genres ORDER BY id ASC";
        try {
            return jdbcTemplate.query(sql, genreRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанры не найдены");
        }
    }

    public Genre getById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
    }

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("full_name"));
        return genre;
    };
}
