package aplication.storage.dao.mappers;

public class LikeRowMapper {
    public static final String ADD_QUERY  = """
        INSERT INTO
            likes (film_id, user_id)
        VALUES
            (?, ?);
    """;

    public static final String DELETE_QUERY = """
       DELETE FROM
            likes
        WHERE
            film_id = ?
            AND user_id = ?;
    """;
}
