package aplication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private long timestamp;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long eventId;
    private Long entityId;

    public enum EventType {
        LIKE, REVIEW, FRIEND;
    }

    public enum OperationType {
        ADD, REMOVE, UPDATE;
    }
}
