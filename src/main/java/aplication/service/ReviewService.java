package aplication.service;

import aplication.exception.NotFoundException;
import aplication.model.Review;
import aplication.storage.ReviewStorage;
import aplication.storage.UserStorage;
import aplication.storage.FilmStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        validateReview(review.getId());
        return reviewStorage.update(review);
    }

    public void delete(long id) {
        reviewStorage.delete(id);
    }

    public Review findById(long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + id + " не найден"));
    }

    public Collection<Review> findAllByFilmId(Long filmId, int count) {
        return reviewStorage.findAllByFilmId(filmId, count);
    }

    public void addReaction(long reviewId, long userId, boolean isLike) {
        validateUser(userId);
        validateReview(reviewId);
        reviewStorage.addReaction(reviewId, userId, isLike);
    }

    public void removeReaction(long reviewId, long userId, boolean isLike) {
        validateUser(userId);
        validateReview(reviewId);
        reviewStorage.removeReaction(reviewId, userId, isLike);
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (filmStorage.getById(filmId) == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    private void validateUser(Long userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void validateReview(Long reviewId) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с ID " + reviewId + " не найден");
        }
    }
}

