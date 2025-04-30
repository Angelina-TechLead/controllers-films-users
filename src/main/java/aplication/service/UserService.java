package aplication.service;

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
        if (userStorage.getAll() != null) {
            return userStorage.getAll();
        } else {
            return new ArrayList<>();
        }
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        user.getFriends().add(friendId);
        userStorage.update(user);
        return user;
    }

    public User update(User user) {
        if (userStorage.getById(user.getId()) != null) {
            userStorage.update(user);
            return user;
        } else {
            return null;
        }
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(user);
        userStorage.update(friend);
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
        return userStorage.getById(userId);
    }

    public Set<User> getFriends(long userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().stream().map(userStorage::getById).collect(Collectors.toSet());
    }

    public User deleteUserById(Long id) {
        if (id == null) throw new IllegalArgumentException("User id не может быть null");
        return userStorage.deleteUserById(id);
    }
}

