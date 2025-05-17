package aplication.storage;

import java.util.List;

import aplication.model.UserEvent;

public interface UserEventStorage {
    UserEvent create(UserEvent userEvent);
    List<UserEvent> getRecentEvents(int userId);
}
