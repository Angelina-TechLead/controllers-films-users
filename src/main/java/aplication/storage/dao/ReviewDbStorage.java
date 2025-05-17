package aplication.storage.dao;

import aplication.model.Review;
import aplication.exception.ReviewAlreadyExistsException;
import aplication.storage.dao.mappers.ReviewMapper;
import aplication.storage.ReviewStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import aplication.storage.dao.queries.ReviewQueryConstants;

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
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(ReviewQueryConstants.ADD_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("review_id")) {
            review.setId(((Number) keys.get("review_id")).longValue());
        } else {
            throw new ReviewAlreadyExistsException("Пользователь уже оставлял отзыв этому фильму.");
        }

        return review;
    }

    @Override
    public Review update(Review review) {
        jdbcTemplate.update(ReviewQueryConstants.UPDATE_QUERY, review.getFilmId(), review.getContent(),
                review.isPositive(), review.getId());
        return review;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(ReviewQueryConstants.DELETE_QUERY, id);
    }

    @Override
    public Optional<Review> findById(long id) {
        return jdbcTemplate.query(ReviewQueryConstants.FIND_BY_ID_QUERY, new ReviewMapper(), id).stream().findFirst();
    }

    @Override
    public Collection<Review> findAllByFilmId(Long filmId, int count) {
        String sql = (filmId == null)
                ? ReviewQueryConstants.GET_REVIEWS_SORTED_BY_USEFULNESS_QUERY
                : ReviewQueryConstants.GET_REVIEWS_BY_FILM_SORTED_BY_USEFULNESS_QUERY;
        return (filmId == null)
                ? jdbcTemplate.query(sql, new ReviewMapper(), count)
                : jdbcTemplate.query(sql, new ReviewMapper(), filmId, count);
    }

    @Override
    public void addReaction(long reviewId, long userId, boolean isLike) {
        jdbcTemplate.update(ReviewQueryConstants.INSERT_OR_UPDATE_REVIEW_REACTION_QUERY, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    @Override
    public void removeReaction(long reviewId, long userId, boolean isLike) {
        jdbcTemplate.update(ReviewQueryConstants.DELETE_REVIEW_REACTIONS_QUERY, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    private void updateUseful(long reviewId) {
        jdbcTemplate.update(ReviewQueryConstants.UPDATE_REVIEW_USEFULNESS_QUERY, reviewId, reviewId);
    }
}
