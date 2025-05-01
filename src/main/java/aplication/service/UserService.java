package aplication.service;

import aplication.exception.NotFoundException;
import aplication.model.User;
import aplication.storage.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        if (userStorage.getAll().isEmpty()) {
            throw new NotFoundException("Пользователи не найдены");
        } else {
            return userStorage.getAll();
        }
    }

    public User add(User user) {
        return userStorage.create(user);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public User update(User u) {
        return userStorage.update(u);
    }

    public void removeFriend(long userId, long friendId) {
      userStorage.removeFriend(userId, friendId);
    }

    public Set<User> getCommonFriends(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        var commonFriendsIds = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .collect(Collectors.toSet());
        if (commonFriendsIds != null && commonFriendsIds.size() > 0) {
            return commonFriendsIds.stream()
                    .map(userStorage::getById)
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    public User getById(long userId) {
        var user = userStorage.getById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь не найдены");
        } else {
            return user;
        }
    }

    public Set<User> getFriends(long userId) {
        var user = userStorage.getById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь не найдены");
        } else {
            return userStorage.getFriends(userId);
        }
    }

    public User deleteUserById(Long id) {
        if (id == null) throw new IllegalArgumentException("User id не может быть null");
        var user = getById(id);
        return userStorage.delete(user);
    }
}

