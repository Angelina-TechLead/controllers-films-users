package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.model.Mpa;
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
public class MpaDbStorage {
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id ASC";
        try {
            return jdbcTemplate.query(sql, mpaRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинги MPA не найдены");
        }
    }

    public Mpa getById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
        }
    }

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("rating_name"));
        return mpa;
    };
}
