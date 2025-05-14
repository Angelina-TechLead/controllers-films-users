package aplication.controller;

import aplication.model.Review;
import aplication.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> create(@Valid @RequestBody Review review) {
        var created = reviewService.create(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        var updated = reviewService.update(review);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reviewService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<Review>> findAllByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        var reviews = reviewService.findAllByFilmId(filmId, count);
        return ResponseEntity.status(HttpStatus.OK).body(reviews);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addReaction(id, userId, true);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addReaction(id, userId, false);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.removeReaction(id, userId, true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.removeReaction(id, userId, false);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

