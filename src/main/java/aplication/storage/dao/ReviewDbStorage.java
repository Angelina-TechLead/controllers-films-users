package aplication.storage.dao;

import aplication.model.Review;
import aplication.storage.ReviewStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);
        review.setId(keyHolder.getKey().longValue());
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
        return jdbcTemplate.query(sql, ReviewMapper::mapRow, id).stream().findFirst();
    }

    @Override
    public Collection<Review> findAllByFilmId(Long filmId, int count) {
        String sql = (filmId == null)
                ? "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return (filmId == null)
                ? jdbcTemplate.query(sql, ReviewMapper::mapRow, count)
                : jdbcTemplate.query(sql, ReviewMapper::mapRow, filmId, count);
    }

    @Override
    public void addReaction(long reviewId, long userId, boolean isLike) {
        String sql = "MERGE INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)";
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

