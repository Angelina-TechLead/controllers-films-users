package aplication.storage.dao.queries;

public class ReviewQueryConstants {
    public static final String ADD_QUERY  = """
        INSERT INTO
            reviews (content, is_positive, user_id, film_id)
        VALUES
            (?, ?, ?, ?) RETURNING review_id
    """;

    public static final String UPDATE_QUERY = """
       UPDATE
           reviews
       SET
           content = ?,
           is_positive = ?
       WHERE
           review_id = ?
    """;

    public static final String DELETE_QUERY = """
       DELETE FROM
           reviews
       WHERE
           review_id = ?
    """;

    public static final String FIND_BY_ID_QUERY = """
       SELECT
           *
       FROM
           reviews
       WHERE
           review_id = ?
    """;

    public static final String DELETE_REVIEW_REACTIONS_QUERY = """
       DELETE FROM
           review_reactions
       WHERE
           review_id = ?
           AND user_id = ?
           AND is_like = ?
    """;

    public static final String UPDATE_REVIEW_USEFULNESS_QUERY = """
        UPDATE
            reviews
        SET
            useful = (
                SELECT
                    COALESCE(
                        SUM(
                            CASE
                                WHEN is_like THEN 1
                                ELSE -1
                            END
                        ),
                        0
                    )
                FROM
                    review_reactions
                WHERE
                    review_id = ?
            )
        WHERE
            review_id = ?
    """;

    public static final String INSERT_OR_UPDATE_REVIEW_REACTION_QUERY = """
        INSERT INTO
            review_reactions (review_id, user_id, is_like)
        VALUES
            (?, ?, ?) ON CONFLICT (review_id, user_id) DO
        UPDATE
        SET
            is_like = EXCLUDED.is_like
    """;

    public static final String GET_REVIEWS_SORTED_BY_USEFULNESS_QUERY = """
       SELECT
           *
       FROM
           reviews
       ORDER BY
           useful DESC
       LIMIT
           ?
    """;

    public static final String GET_REVIEWS_BY_FILM_SORTED_BY_USEFULNESS_QUERY = """
        SELECT
            *
        FROM
            reviews
        WHERE
            film_id = ?
        ORDER BY
            useful DESC
        LIMIT
            ?
    """;
}
