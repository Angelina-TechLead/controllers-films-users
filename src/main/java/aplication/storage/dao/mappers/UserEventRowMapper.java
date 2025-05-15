package aplication.storage.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import aplication.model.UserEvent;
import aplication.model.UserEvent.EventType;
import aplication.model.UserEvent.OperationType;

public class UserEventRowMapper implements RowMapper<UserEvent> {
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

    @Override
    @SuppressWarnings("null")
    public UserEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserEvent(
            rs.getLong("timestamp"),
            rs.getLong("userId"),
            EventType.valueOf(rs.getString("eventType")),
            OperationType.valueOf(rs.getString("operation")),
            rs.getLong("eventId"),
            rs.getLong("entityId"));
    }
}
