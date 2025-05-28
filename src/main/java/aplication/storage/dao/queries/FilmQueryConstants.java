package aplication.storage.dao.queries;

public class FilmQueryConstants {
    public static final String GET_FILMS_QUERY = """
        SELECT
            f.id,
            f.film_name AS name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa_id AS rating_id,
            r.rating_name,
            ARRAY_AGG(
                DISTINCT l.user_id
                ORDER BY
                    l.user_id ASC
            ) AS likes,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', g.id, 'name', g.full_name)
            ) FILTER (
                WHERE
                    g.id IS NOT NULL
            ) AS genres,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', d.id, 'name', d.director_name)
            ) FILTER (
                WHERE
                    d.id IS NOT NULL
            ) AS directors
        FROM
            films AS f
            LEFT JOIN film_directors AS fd ON f.id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.id
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN film_genres AS fg ON f.id = fg.film_id
            LEFT JOIN genres AS g ON g.id = fg.genre_id
            LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
        GROUP BY
            f.id,
            r.rating_name;
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
           ARRAY_AGG(
               DISTINCT l.user_id
               ORDER BY
                   l.user_id ASC
           ) AS likes,
           JSONB_AGG(
               JSONB_BUILD_OBJECT('id', g.id, 'name', g.full_name)
           ) FILTER (
               WHERE
                   g.id IS NOT NULL
           ) AS genres,
           JSONB_AGG(
               JSONB_BUILD_OBJECT('id', d.id, 'name', d.director_name)
           ) FILTER (
               WHERE
                   d.id IS NOT NULL
           ) AS directors
       FROM
           films AS f
           LEFT JOIN film_directors AS fd ON f.id = fd.film_id
           LEFT JOIN directors AS d ON fd.director_id = d.id
           LEFT JOIN likes AS l ON f.id = l.film_id
           LEFT JOIN film_genres AS fg ON f.id = fg.film_id
           LEFT JOIN genres AS g ON g.id = fg.genre_id
           LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
       WHERE
           d.id = ?
       GROUP BY
           f.id,
           r.rating_name
       ORDER BY
           f.release_date
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
          ARRAY_AGG(
              DISTINCT l.user_id
              ORDER BY
                  l.user_id ASC
          ) AS likes,
          JSONB_AGG(
              JSONB_BUILD_OBJECT('id', g.id, 'name', g.full_name)
          ) FILTER (
              WHERE
                  g.id IS NOT NULL
          ) AS genres,
          JSONB_AGG(
              JSONB_BUILD_OBJECT('id', d.id, 'name', d.director_name)
          ) FILTER (
              WHERE
                  d.id IS NOT NULL
          ) AS directors,
          COUNT(DISTINCT l.user_id) AS likes_count
      FROM
          films AS f
          LEFT JOIN film_directors AS fd ON f.id = fd.film_id
          LEFT JOIN directors AS d ON fd.director_id = d.id
          LEFT JOIN likes AS l ON f.id = l.film_id
          LEFT JOIN film_genres AS fg ON f.id = fg.film_id
          LEFT JOIN genres AS g ON g.id = fg.genre_id
          LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
      WHERE
          d.id = ?
      GROUP BY
          f.id,
          r.rating_name
      ORDER BY
          likes_count DESC,
          f.release_date DESC
    """;

    public static final String DELETE_FILM_BY_ID_QUERY  = """
        DELETE FROM
            films
        WHERE
            id = ?;
    """;

    public static final String GET_SIMPLE_FILM_QUERY = """
        SELECT
            f.id,
            f.film_name AS name,
            f.description,
            f.release_date,
            f.duration,
            NULL AS rating_id,
            NULL AS rating_name,
            NULL AS likes,
            NULL AS genres
            NULL AS directors
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
            ARRAY_AGG(
                DISTINCT l.user_id
                ORDER BY
                    l.user_id ASC
            ) AS likes,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', g.id, 'name', g.full_name)
            ) FILTER (
                WHERE
                    g.id IS NOT NULL
            ) AS genres,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', d.id, 'name', d.director_name)
            ) FILTER (
                WHERE
                    d.id IS NOT NULL
            ) AS directors
        FROM
            films AS f
            LEFT JOIN film_directors AS fd ON f.id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.id
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN film_genres AS fg ON f.id = fg.film_id
            LEFT JOIN genres AS g ON g.id = fg.genre_id
            LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
        WHERE
            f.id = ?
        GROUP BY
            f.id,
            r.rating_name;
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

    public static final String RECOMMENDATIONS_QUERY = """
        SELECT L.film_id
        FROM (
            SELECT L2.user_id, COUNT(*) AS cnt
            FROM likes L1
            JOIN likes L2 ON L1.film_id = L2.film_id
            WHERE L1.user_id = ?
            AND L2.user_id <> L1.user_id
            GROUP BY L2.user_id
            ORDER BY cnt DESC
            LIMIT 1
        ) AS U
        JOIN likes L ON U.user_id = L.user_id
        WHERE L.film_id NOT IN (
            SELECT film_id FROM likes WHERE user_id = ?
        )
        LIMIT ?;
    """;

    public static final String ADD_FILM_DIRECTOR_QUERY = """
        INSERT INTO
            film_directors (film_id, director_id)
        VALUES
            (?, ?)
        """;

    public static final String REMOVE_FILM_DIRECTOR_QUERY = """
        DELETE FROM
            film_directors
        WHERE
            film_id = ?
    """;

    public static final String GET_COMMON_FILMS_QUERY = """
        SELECT
            f.id,
            f.film_name AS name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa_id AS rating_id,
            r.rating_name,
            ARRAY_AGG(
                DISTINCT l.user_id
                ORDER BY
                    l.user_id ASC
            ) AS likes,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', g.id, 'name', g.full_name)
            ) FILTER (
                WHERE
                    g.id IS NOT NULL
            ) AS genres,
            JSONB_AGG(
                JSONB_BUILD_OBJECT('id', d.id, 'name', d.director_name)
            ) FILTER (
                WHERE
                    d.id IS NOT NULL
            ) AS directors
        FROM
            films AS f
            LEFT JOIN film_directors AS fd ON f.id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.id
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN film_genres AS fg ON f.id = fg.film_id
            LEFT JOIN genres AS g ON g.id = fg.genre_id
            LEFT JOIN mpa_ratings AS r ON f.mpa_id = r.id
        WHERE
            f.id IN (
                SELECT
                    film_id
                FROM
                    likes
                WHERE
                    user_id = ?
                INTERSECT
                SELECT
                    film_id
                FROM
                    likes
                WHERE
                    user_id = ?
            )
        GROUP BY
            f.id,
            r.rating_name
        ORDER BY
            COUNT(DISTINCT l.user_id) DESC;
    """;
}
