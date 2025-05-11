package aplication.storage.dao.mappers;

import aplication.model.Director;
import aplication.model.Film;
import aplication.model.Genre;
import aplication.model.Mpa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FilmRowMapper implements RowMapper<Film> {
    public static final String GET_FILMS_QUERY = """
            SELECT
                f.id AS id,
                f.film_name AS name,
                f.description AS description,
                f.release_date AS release_date,
                f.duration AS duration,
                f.mpa_id AS rating_id,
                r.rating_name AS rating_name,
                ARRAY_AGG(DISTINCT l.user_id) AS likes,
                CAST(
                    JSON_ARRAYAGG(
                        DISTINCT JSON_OBJECT(
                            'id': g.id,
                            'name': g.full_name
                        )
                    ) FILTER (
                        WHERE
                            g.id IS NOT NULL
                    ) AS VARCHAR
                ) AS genres
            FROM
                films AS f
                LEFT JOIN likes AS l ON f.id = l.film_id
                LEFT JOIN film_genres AS fg ON f.id = fg.film_id
                LEFT JOIN genres AS g ON g.id = fg.genre_id
                LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
            GROUP BY
                f.id,
                r.rating_name;
            """;

    public static final String GET_POPULAR_FILMS_QUERY  = """
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
                            'id': g.id,
                            'name': g.full_name
                        )
                    ) FILTER (
                        WHERE
                            g.id IS NOT NULL
                    ) AS VARCHAR
                ) AS genres
            FROM
                films AS f
                LEFT JOIN likes AS l ON f.id = l.film_id
                LEFT JOIN film_genres AS fg ON f.id = fg.film_id
                LEFT JOIN genres AS g ON g.id = fg.genre_id
                LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
            GROUP BY
                f.id
            ORDER BY
                COUNT(DISTINCT l.user_id) DESC
            LIMIT
                ?;
    """;

    public static final String GET_DIRECTOR_FILMS_SORTED_BY_YEAR = """
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
                    'id': g.id,
                    'name': g.full_name
                )
            ) FILTER (WHERE g.id IS NOT NULL) AS VARCHAR
        ) AS genres,
        CAST(
            JSON_ARRAYAGG(
                DISTINCT JSON_OBJECT(
                    'id': d.id,
                    'name': d.director_name
                )
            ) FILTER (WHERE d.id IS NOT NULL) AS VARCHAR
        ) AS directors
    FROM films AS f
    JOIN film_directors AS fd ON f.id = fd.film_id
    JOIN directors AS d ON fd.director_id = d.id
    LEFT JOIN likes AS l ON f.id = l.film_id
    LEFT JOIN film_genres AS fg ON f.id = fg.film_id
    LEFT JOIN genres AS g ON g.id = fg.genre_id
    LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
    WHERE d.id = ?
    GROUP BY f.id, r.rating_name
    ORDER BY f.release_date
    """;

    public static final String GET_DIRECTOR_FILMS_SORTED_BY_LIKES = """
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
                    'id': g.id,
                    'name': g.full_name
                )
            ) FILTER (WHERE g.id IS NOT NULL) AS VARCHAR
        ) AS genres,
        CAST(
            JSON_ARRAYAGG(
                DISTINCT JSON_OBJECT(
                    'id': d.id,
                    'name': d.director_name
                )
            ) FILTER (WHERE d.id IS NOT NULL) AS VARCHAR
        ) AS directors,
        COUNT(DISTINCT l.user_id) AS likes_count
    FROM films AS f
    JOIN film_directors AS fd ON f.id = fd.film_id
    JOIN directors AS d ON fd.director_id = d.id
    LEFT JOIN likes AS l ON f.id = l.film_id
    LEFT JOIN film_genres AS fg ON f.id = fg.film_id
    LEFT JOIN genres AS g ON g.id = fg.genre_id
    LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
    WHERE d.id = ?
    GROUP BY f.id, r.rating_name
    ORDER BY likes_count DESC, f.release_date DESC
    """;

    public static final String DELETE_FILM_BY_ID_QUERY  = """
        DELETE FROM
            films
        WHERE
            id = ?;
    """;

    public static final String GET_SIMPLE_FILM_QUERY = """
        SELECT
            f.id AS id,
            f.film_name AS name,
            f.description AS description,
            f.release_date AS release_date,
            f.duration AS duration,
            NULL AS rating_id,
            NULL AS rating_name,
            NULL AS likes,
            NULL AS genres
        FROM
            films AS f
        WHERE
            f.id = ?;
    """;

    public static final String GET_FILM_BY_ID_QUERY  = """
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
                        'id': g.id,
                        'name': g.full_name
                    )
                ) FILTER (
                    WHERE
                        g.id IS NOT NULL
                ) AS VARCHAR
            ) AS genres
        FROM
            films AS f
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN film_genres AS fg ON f.id = fg.film_id
            LEFT JOIN genres AS g ON g.id = fg.genre_id
            LEFT JOIN mpa_ratings AS r ON f.mpa_id= r.id
        WHERE
            f.id = ?
        GROUP BY
            f.id;
    """;

    public static final String CREATE_FILM_QUERY  = """
        INSERT INTO
            films (
                film_name,
                description,
                release_date,
                duration,
                mpa_id
            )
        VALUES
            (?, ?, ?, ?, ?);
    """;

    public static final String ADD_FILM_GENRE_QUERY  = """
        INSERT INTO
            film_genres (film_id, genre_id)
        VALUES
            (?, ?);
    """;

    public static final String UPDATE_FILM_QUERY  = """
        UPDATE
            films
        SET
            film_name = ?,
            description = ?,
            release_date = ?,
            duration = ?,
            mpa_id = ?
        WHERE
            id = ?;
    """;

    public static final String REMOVE_FILM_GENRES_QUERY  = """
        DELETE FROM
            film_genres
        WHERE
            film_id = ?;
    """;

    public static final String ADD_FILM_DIRECTOR_QUERY = """
        INSERT INTO
            film_directors (film_id, director_id)
        VALUES
            (?, ?)
        """;

    public static final String REMOVE_FILM_DIRECTORS_QUERY  = """
        DELETE FROM
            film_directors
        WHERE
            film_id = ?
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

        // Обработка жанров
        String dbGenres = rs.getString("genres");
        if (dbGenres != null && !dbGenres.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Genre> filmGenres = objectMapper.readValue(dbGenres, new TypeReference<Set<Genre>>() {});
                film.setGenres(filmGenres);
            } catch (JsonProcessingException e) {
                // Логирование ошибки
            }
        }

        // Обработка режиссеров
        String dbDirectors = rs.getString("directors");
        if (dbDirectors != null && !dbDirectors.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Director> filmDirectors = objectMapper.readValue(dbDirectors, new TypeReference<Set<Director>>() {});
                film.setDirectors(filmDirectors);
            } catch (JsonProcessingException e) {
                // Логирование ошибки
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