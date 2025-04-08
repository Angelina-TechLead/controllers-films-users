package aplication.storage;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();
    private long currentId = 1;

    public User add(User user) {
        user.setId(currentId++);
        users.add(user);
        return user;
    }

    public void delete(long id) {
        users.removeIf(user -> user.getId() == id);
    }

    public User update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                return user;
            }
        }
        throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
    }

    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    public User getById(long id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}