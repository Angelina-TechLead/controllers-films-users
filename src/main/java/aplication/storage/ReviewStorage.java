package aplication.storage;

import aplication.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(long id);

    Optional<Review> findById(long id);

    Collection<Review> findAllByFilmId(Long filmId, int count);

    void addReaction(long reviewId, long userId, boolean isLike);

    void removeReaction(long reviewId, long userId, boolean isLike);
}

