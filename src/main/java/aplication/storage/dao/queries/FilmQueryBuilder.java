package aplication.storage.dao.queries;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class FilmQueryBuilder {
    private static final String BASE_QUERY = """
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
    """;

    private final StringBuilder whereClause = new StringBuilder();
    private final StringBuilder orderClause = new StringBuilder(" ORDER BY COUNT(l.user_id) DESC");
    @Getter
    private final Map<String, Object> parameters = new HashMap<>();
    private int limit = 10;

    public FilmQueryBuilder addFilters(Map<String, Object> filters) {
        if (filters.containsKey("title")) {
            appendCondition("f.film_name ILIKE CONCAT('%', :title, '%')");
            parameters.put("title", filters.get("title"));
        }
        if (filters.containsKey("director")) {
            appendCondition("d.director_name ILIKE CONCAT('%', :director, '%')");
            parameters.put("director", filters.get("director"));
        }
        if (filters.containsKey("genreId")) {
            Integer genreId = Integer.parseInt(filters.get("genreId").toString());
            appendCondition("fg.genre_id = :genre_id");
            parameters.put("genre_id", genreId);
        }
        if (filters.containsKey("year")) {
            Integer year = Integer.parseInt(filters.get("year").toString());
            appendCondition("EXTRACT(YEAR FROM f.release_date) = :year");
            parameters.put("year", year);
        }
        if (filters.containsKey("count")) {
            limit = Integer.parseInt(filters.get("count").toString());
        }
        return this;
    }

    private void appendCondition(String condition) {
        if (whereClause.isEmpty()) {
            whereClause.append(" WHERE ").append(condition);
        } else {
            whereClause.append(" OR ").append(condition);
        }
    }

    public String buildQuery() {
        return BASE_QUERY + whereClause + " GROUP BY f.id, r.rating_name" + orderClause + " LIMIT " + limit;
    }
}
