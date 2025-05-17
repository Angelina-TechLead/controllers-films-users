package aplication.storage.dao.queries;

public class UserEventQueryConstants {
    public static final String GET_FEEDS = """
        SELECT
            id AS eventId,
            user_id AS userId,
            event_type AS eventType,
            operation AS operation,
            entity_id AS entityId,
            EXTRACT(
                EPOCH
                FROM
                    timestamp
            ) AS timestamp
        FROM
            user_events
        WHERE
            user_id = ?
        ORDER BY
            timestamp DESC
    """;

    public static final String CREATE_QUERY = """
        INSERT INTO
            user_events (user_id, event_type, operation, entity_id)
        VALUES
            (?, ?::event_type_enum, ?::operation_type_enum, ?)
        RETURNING
            id;
    """;
}
