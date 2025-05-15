package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.User;
import aplication.storage.UserStorage;

import aplication.storage.dao.mappers.FilmRowMapper;
import aplication.storage.dao.mappers.UserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Primary
@Component
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(UserRowMapper.CREATE_USER_QUERY, new String[]{"id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                if (user.getBirthday() != null) {
                    ps.setDate(4, Date.valueOf(user.getBirthday()));
                } else {
                    ps.setNull(4, Types.DATE);
                }
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Пользователь с таким email или логином уже существует");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не может быть создан");
        }
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        try {
            jdbc.update(UserRowMapper.UPDATE_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Ошибка при обновлении пользователя");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Элемент с ID " + user.getId() + " не может быть обновлен");
        }
        return getById(user.getId());
    }

    @Override
    @Transactional
    public User delete(User user) {
        jdbc.update(UserRowMapper.DELETE_USER_BY_ID_QUERY, user.getId());
        return user;
    }

    @Override
    public User getById(long id) {
        try {
            return jdbc.queryForObject(UserRowMapper.GET_USER_BY_ID_QUERY, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public List<User> getAll() {
        return jdbc.query(UserRowMapper.GET_USERS_QUERY, new UserRowMapper());
    }

    @Override
    public Set<User> getFriends(long userId) {
        return new HashSet<>(jdbc.query(UserRowMapper.GET_USER_FRIENDS, new UserRowMapper(), userId));
    }

    @Override
    public void existsById(Long id) {
        try {
            jdbc.queryForObject(UserRowMapper.GET_SIMPLE_USER_QUERY, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long fromId, Long toId) {
        existsById(fromId);
        existsById(toId);
        try {
            jdbc.update(UserRowMapper.ADD_FRIEND_QUERY, fromId, toId);
        } catch (DuplicateKeyException e) {
            throw new NotFoundException("Невозможно добавить дружбу между пользователями с ID " + fromId + " и " + toId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        existsById(userId);
        existsById(friendId);
        jdbc.update(UserRowMapper.REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<Long> getRecommendations(Long id, Integer count) {
        return jdbc.query(FilmRowMapper.RECOMMENDATIONS_QUERY,
                (rs, rowNum) -> rs.getLong("film_id"), id, id, count);
    }
}
