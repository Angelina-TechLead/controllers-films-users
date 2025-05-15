package aplication.storage.dao;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import aplication.exception.NotFoundException;
import aplication.exception.ValidationException;
import aplication.model.UserEvent;
import aplication.storage.UserEventStorage;
import aplication.storage.dao.mappers.UserEventRowMapper;

@Primary
@Component
@Repository
@RequiredArgsConstructor
public class UserEventDbStorage implements UserEventStorage {
    private final JdbcTemplate jdbc;

    @Override
    public List<UserEvent> getRecentEvents(int userId) {
        return jdbc.query(UserEventRowMapper.GET_FEEDS, new UserEventRowMapper(), userId);
    }

    @Override
    public UserEvent create(UserEvent userEvent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(
                        UserEventRowMapper.CREATE_QUERY,
                        new String[] { "id" });
                ps.setLong(1, userEvent.getUserId());
                ps.setString(2, userEvent.getEventType().name());
                ps.setString(3, userEvent.getOperation().name());
                ps.setLong(4, userEvent.getEntityId());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Событие с такими данными уже существует");
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Ошибка при создании события");
        }

        userEvent.setEventId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return userEvent;
    }
}
