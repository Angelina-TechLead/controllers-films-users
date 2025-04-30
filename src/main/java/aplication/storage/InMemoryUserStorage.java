package aplication.storage;

import aplication.exception.NotFoundException;
import aplication.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();
    private long currentId = 1;

    public User add(User user) {
        user.setId(currentId++);
        users.add(user);
        return user;
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

    @Override
    public User deleteUserById(long id) {
        User userToDelete = users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElse(null);

        if (userToDelete != null) {
            users.remove(userToDelete);
        }

        return userToDelete;
    }
}