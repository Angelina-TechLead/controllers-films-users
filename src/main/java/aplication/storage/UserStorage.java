package aplication.storage;

import aplication.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {
    User create(User user);
    User update(User user);
    User delete(User user);

    List<User> getAll();

    Set<User> getFriends(long userId);
    User getById(long id);

    void existsById(Long id);
    void addFriend(Long fromId, Long toId);
    void removeFriend(long userId, long friendId);

    Collection<Long> getRecommendations(Long id, Integer count);
}
