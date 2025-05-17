package aplication.storage.dao.queries;

public class UserQueryConstants {
    public static final String GET_USERS_QUERY = """
        SELECT
            u.id AS id,
            u.email AS email,
            u.login AS login,
            u.username AS name,
            u.birthday AS birthday,
            ARRAY_AGG(DISTINCT f.friend_id) AS friends
        FROM
            users AS u
            LEFT JOIN friends AS f ON u.id = f.user_id
        GROUP BY
            u.id;
    """;

    public static final String GET_USER_BY_ID_QUERY = """
        SELECT
           u.id,
           u.email,
           u.login,
           u.username AS name,
           u.birthday AS birthday,
           ARRAY_AGG(DISTINCT f.friend_id) AS friends
       FROM
           users AS u
           LEFT JOIN friends AS f ON u.id = f.user_id
       WHERE
           u.id = ?
       GROUP BY
           u.id;
    """;

    public static final String GET_SIMPLE_USER_QUERY = """
        SELECT
            u.id AS id,
            u.email AS email,
            u.login AS login,
            u.username AS name,
            u.birthday AS birthday,
            NULL AS friends
        FROM
            users AS u
        WHERE
            u.id = ?;
    """;

    public static final String GET_USER_FRIENDS = """
        SELECT
            u.id,
            u.email,
            u.login,
            u.username AS name,
            u.birthday,
            NULL AS friends
        FROM
            users AS u
            JOIN friends f ON u.id = f.friend_id
        WHERE
            f.user_id = ?
        GROUP BY
            u.id;
    """;

    public static final String DELETE_USER_BY_ID_QUERY = """
        DELETE FROM
            users
        WHERE
            user_id = ?;
    """;

    public static final String CREATE_USER_QUERY = """
        INSERT INTO
            users (email, login, username, birthday)
        VALUES
            (?, ?, ?, ?);
    """;

    public static final String UPDATE_USER_QUERY = """
        UPDATE
            users
        SET
            email = ?,
            login = ?,
            username = ?,
            birthday = ?
        WHERE
            id = ?;
    """;

    public static final String ADD_FRIEND_QUERY = """
        INSERT INTO
            friends (user_id, friend_id)
        VALUES
            (?, ?);
    """;

    public static final String REMOVE_FRIEND_QUERY = """
        DELETE FROM
            friends
        WHERE
            user_id = ?
            AND friend_id = ?;
    """;
}
