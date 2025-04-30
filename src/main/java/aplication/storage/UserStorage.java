package aplication.storage;

import aplication.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    List<User> getAll();

    User getById(long id);

    User deleteUserById(long id);
}

