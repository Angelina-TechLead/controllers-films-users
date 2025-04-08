package aplication.storage;

import aplication.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    void delete(long id);

    List<User> getAll();

    User getById(long id);
}

