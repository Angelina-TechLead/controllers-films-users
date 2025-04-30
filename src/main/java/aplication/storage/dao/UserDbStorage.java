package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.model.User;
import aplication.storage.UserStorage;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Primary
@Component
@Repository
public class UserDbStorage implements UserStorage {
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, username, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        String sqlGetId = "SELECT id FROM users WHERE email = ?";
        Long id = jdbcTemplate.queryForObject(sqlGetId, Long.class, user.getEmail());
        user.setId(id);
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        // Обновляем основные данные пользователя
        String sql = "UPDATE users SET email = ?, login = ?, username = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        // Обновляем друзей пользователя
        List<Long> existingFriends = getFriendsIds(user.getId()); // Получаем текущих друзей пользователя

        // Удаляем тех друзей, которые больше не числятся у пользователя
        for (Long friendId : existingFriends) {
            if (!user.getFriends().contains(friendId)) {
                removeFriend(user.getId(), friendId);
            }
        }

        // Добавляем новых друзей
        for (Long friendId : user.getFriends()) {
            if (!existingFriends.contains(friendId)) {
                addFriend(user.getId(), friendId);
            }
        }

        return user;
    }

    @Override
    @Transactional
    public User deleteUserById(long userId) {
        var user = getById(userId);

        // Удаляем записи о дружбе из таблицы friend
        String deleteFriendsSql = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFriendsSql, userId, userId);

        // Удаляем пользователя из таблицы users
        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(deleteUserSql, userId);

        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return user;
    }

    @Override
    public User getById(long userId) {
        try {
            // Загружаем информацию о пользователе
            String sql = "SELECT * FROM users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);

            // Загружаем список друзей
            String friendsSql = "SELECT friend_id FROM friends WHERE user_id = ?";
            List<Long> friendIds = jdbcTemplate.queryForList(friendsSql, Long.class, userId);
            user.setFriends(new HashSet<>(friendIds)); // Устанавливаем список друзей для пользователя

            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        for (User user : users) {
            String friendsSql = "SELECT friend_id FROM friends WHERE user_id = ?";
            List<Long> friendIds = jdbcTemplate.queryForList(friendsSql, Long.class, user.getId());
            user.setFriends(new HashSet<>(friendIds)); // Устанавливаем друзей для пользователя
        }

        return users;
    }

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("username")); // Изменено на корректное название столбца
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    private List<Long> getFriendsIds(long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    private void addFriend(long userId, long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    private void removeFriend(long userId, long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }
}
