package aplication.storage.dao.mappers;

import aplication.model.Director;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectorRowMapper implements RowMapper<Director> {
    public static final String GET_ALL_QUERY = "SELECT id, director_name AS name FROM directors";
    public static final String GET_BY_ID_QUERY = "SELECT id, director_name AS name FROM directors WHERE id = ?";
    public static final String CREATE_QUERY = "INSERT INTO directors (director_name) VALUES (?)";
    public static final String UPDATE_QUERY = "UPDATE directors SET director_name = ? WHERE id = ?";
    public static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";
    public static final String GET_FILMS_BY_DIRECTOR = """
        SELECT f.id, f.film_name, f.description, f.release_date, f.duration 
        FROM films f
        JOIN film_directors fd ON f.id = fd.film_id
        WHERE fd.director_id = ?
        """;

    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    }
}