package aplication.storage;

import aplication.exception.NotFoundException;
import aplication.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();
    private long currentId = 1;

    public User create(User user) {
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

    @Override
    public Set<User> getFriends(long userId) {
        return Set.of();
    }

    public User getById(long id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    public void existsById(Long id) {
        users.stream()
                .anyMatch(user -> user.getId() == id);
    }

    @Override
    public void addFriend(Long fromId, Long toId) {
        User user = getById(fromId);
        User friend = getById(toId);

        user.getFriends().add(toId);
        friend.getFriends().add(fromId);

        update(user);
        update(friend);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        update(user);
        update(friend);
    }

    @Override
    public User delete(User user) {
        if (user != null) {
            users.remove(user);
        }

        return user;
    }
}