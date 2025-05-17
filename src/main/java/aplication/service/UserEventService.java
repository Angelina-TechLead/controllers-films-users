package aplication.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aplication.exception.NotFoundException;
import aplication.model.UserEvent;
import aplication.storage.UserEventStorage;
import aplication.storage.UserStorage;

@Service
public class UserEventService {
    private final UserStorage userStorage;
    private final UserEventStorage userEventStorage;

    @Autowired
    public UserEventService(UserEventStorage userEventStorage, UserStorage userStorage) {
        this.userEventStorage = userEventStorage;
        this.userStorage = userStorage;
    }

    public UserEvent create(UserEvent userEvent) {
        return userEventStorage.create(userEvent);
    }

    public List<UserEvent> getFeeds(int id) {
        var user = userStorage.getById(id);

        if (user == null) {
            throw new NotFoundException("Пользователь не найдены");
        } else {
            return userEventStorage.getRecentEvents(id);
        }
    }
}
