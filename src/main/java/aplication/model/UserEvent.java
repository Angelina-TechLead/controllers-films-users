package aplication.model;

import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long userId;

    @NotNull
    private EventType eventType;

    @NotNull
    private OperationType operation;

    @NotNull
    private Long eventId;

    @NotNull
    private Long entityId;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum OperationType {
        ADD, REMOVE, UPDATE
    }
}
