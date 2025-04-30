package aplication.storage.dao.mappers;

import aplication.model.Film;
import aplication.model.Genre;
import aplication.model.Mpa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FilmRowMapper implements RowMapper<Film> {

    public static String GET_POPULAR_FILMS_QUERY = """
            SELECT
                f.id,
                f.film_name AS name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id AS rating_id,
                r.rating_name,
                ARRAY_AGG(DISTINCT l.user_id) AS likes,
                CAST(
                    JSON_ARRAYAGG(
                        DISTINCT JSON_OBJECT(
                            'id' : g.id,
                            'name' : g.full_name
                        )
                    ) FILTER (WHERE g.id IS NOT NULL) AS VARCHAR
                ) AS genres
            FROM films AS f
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN film_genres AS fg ON f.id = fg.film_id
            LEFT JOIN genres AS g ON g.id = fg.genre_id
            LEFT JOIN ratings AS r ON f.mpa_id = r.id
            GROUP BY f.id
            ORDER BY COUNT(DISTINCT l.user_id) DESC
            LIMIT ?;
            """;

    public static String GET_FILMS_QUERY = """
            SELECT
                f.id AS id,
                f.film_name AS name,
                f.description AS description,
                f.release_date AS release_date,
                f.duration AS duration,
                f.rating_id AS rating_id,
                r.rating_name AS rating_name,
                ARRAY_AGG(DISTINCT l.user_id) AS likes,
                CAST(
                    JSON_ARRAYAGG(
                        DISTINCT JSON_OBJECT(
                            'id' : g.id,
                            'name' : g.full_name
                        )
                    ) FILTER (WHERE g.genre_id IS NOT NULL) AS VARCHAR
                ) AS genres
            FROM films AS f
            LEFT JOIN likes AS l ON f.film_id = l.film_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            GROUP BY f.film_id;
            """;

    public static String GET_FILM_BY_ID_QUERY = """
            SELECT
                f.film_id AS id,
                f.name AS name,
                f.description AS description,
                f.release_date AS release_date,
                f.duration AS duration,
                f.rating_id AS rating_id,
                r.name AS rating_name,
                ARRAY_AGG(DISTINCT l.user_id) AS likes,
                CAST(
                    JSON_ARRAYAGG(
                        DISTINCT JSON_OBJECT(
                            'id' : g.genre_id,
                            'name' : g.name
                        )
                    ) FILTER (WHERE g.genre_id IS NOT NULL) AS VARCHAR
                ) AS genres
            FROM films AS f
            LEFT JOIN likes AS l ON f.film_id = l.film_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            WHERE f.film_id = ?
            GROUP BY f.film_id;
            """;

    public static String GET_SIMPLE_FILM_QUERY = """
            SELECT
                f.film_id AS id,
                f.name AS name,
                f.description AS description,
                f.release_date AS release_date,
                f.duration AS duration,
                NULL AS rating_id,
                NULL AS rating_name,
                NULL AS likes,
                NULL AS genres
            FROM films AS f
            WHERE f.film_id = ?;
            """;

    public static String DELETE_FILM_BY_ID_QUERY = """
            DELETE FROM films
            WHERE film_id = ?;
            """;

    public static String CREATE_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, rating_id)
            VALUES (?, ?, ?, ?, ?);
            """;

    public static String UPDATE_FILM_QUERY = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
            WHERE film_id = ?;
            """;

    public static String ADD_FILM_GENRE_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
            """;

    public static String REMOVE_FILM_GENRES_QUERY = """
            DELETE FROM film_genres WHERE film_id = ?;
            """;

    public static String ADD_LIKE_QUERY = """
            INSERT INTO likes (film_id, user_id)
            VALUES (?, ?);
            """;

    public static String REMOVE_LIKE_QUERY = """
            DELETE FROM likes
            WHERE film_id = ? AND user_id = ?;
            """;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        var film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        if (rs.getDate("release_date") != null) {
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }

        var dbDuration = rs.getObject("duration", Long.class);
        if (dbDuration != null) {
            film.setDuration(dbDuration);
        }

        var dbRating = rs.getObject("rating_id", Integer.class);
        if (dbRating != null) {
            Mpa filmRating = new Mpa();
            filmRating.setId(dbRating);
            filmRating.setName(rs.getString("rating_name"));
            film.setMpa(filmRating);
        }

        film.setLikes(makeLongSet(rs.getArray("likes")));

        String dbGenres = rs.getString("genres");
        if (dbGenres != null && !dbGenres.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Genre> filmGenres = objectMapper.readValue(dbGenres, new TypeReference<Set<Genre>>() {
                });
                film.setGenres(filmGenres);
            } catch (JsonProcessingException e) {
                // do nothing
            }

        }
        return film;
    }


    private Set<Long> makeLongSet(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) return new HashSet<>();
        Object[] objectArray = (Object[]) sqlArray.getArray();
        return Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toSet());
    }
}