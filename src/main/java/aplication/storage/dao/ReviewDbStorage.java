package aplication.storage.dao;

import aplication.model.Review;
import aplication.storage.dao.mappers.ReviewMapper;
import aplication.storage.ReviewStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?) RETURNING review_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        // Проверяем, есть ли возвращённые ключи и извлекаем "review_id"
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("review_id")) {
            review.setId(((Number) keys.get("review_id")).longValue());
        } else {
            throw new RuntimeException("Не удалось получить review_id после вставки");
        }

        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.isPositive(), review.getId());
        return review;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public Optional<Review> findById(long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        return jdbcTemplate.query(sql, new ReviewMapper(), id).stream().findFirst();
    }

    @Override
    public Collection<Review> findAllByFilmId(Long filmId, int count) {
        String sql = (filmId == null)
                ? "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return (filmId == null)
                ? jdbcTemplate.query(sql, new ReviewMapper(), count)
                : jdbcTemplate.query(sql, new ReviewMapper(), filmId, count);
    }

    @Override
    public void addReaction(long reviewId, long userId, boolean isLike) {
        String sql = "INSERT INTO review_reactions (review_id, user_id, is_like)\n" +
                "VALUES (?, ?, ?)\n" +
                "ON CONFLICT (review_id, user_id) \n" +
                "DO UPDATE SET is_like = EXCLUDED.is_like;";
        jdbcTemplate.update(sql, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    @Override
    public void removeReaction(long reviewId, long userId, boolean isLike) {
        String sql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = ?";
        jdbcTemplate.update(sql, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    private void updateUseful(long reviewId) {
        String sql = """
                UPDATE reviews SET useful = (
                    SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0)
                    FROM review_reactions WHERE review_id = ?
                ) WHERE review_id = ?
                """;
        jdbcTemplate.update(sql, reviewId, reviewId);
    }
}
