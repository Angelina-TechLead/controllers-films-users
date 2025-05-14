package aplication.storage.dao;

import aplication.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewMapper {
    public static Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .positive(rs.getBoolean("is_positive")) // <-- ВАЖНО!
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}

