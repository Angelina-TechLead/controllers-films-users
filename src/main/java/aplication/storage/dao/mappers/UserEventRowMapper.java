package aplication.storage.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import aplication.model.UserEvent;
import aplication.model.UserEvent.EventType;
import aplication.model.UserEvent.OperationType;

public class UserEventRowMapper implements RowMapper<UserEvent> {
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
