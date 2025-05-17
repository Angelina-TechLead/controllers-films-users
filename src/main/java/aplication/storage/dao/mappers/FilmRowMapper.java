package aplication.storage.dao.mappers;

import aplication.model.Director;
import aplication.model.Film;
import aplication.model.Genre;
import aplication.model.Mpa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        var film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        if (rs.getDate("release_date") != null) {
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }

        var dbDuration = rs.getLong("duration");
        if (!rs.wasNull()) {
            film.setDuration(dbDuration);
        }

        var dbRating = rs.getObject("rating_id", Integer.class);
        if (dbRating != null) {
            Mpa filmRating = new Mpa();
            filmRating.setId(dbRating);
            filmRating.setName(rs.getString("rating_name"));
            film.setMpa(filmRating);
        }

        film.setLikes(makeLongSet(rs.getArray("likes")));

        String dbGenres = rs.getString("genres");
        if (dbGenres != null && !dbGenres.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Genre> filmGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
                filmGenres.addAll(objectMapper.readValue(dbGenres, new TypeReference<Set<Genre>>() {}));
                film.setGenres(filmGenres);
            } catch (JsonProcessingException e) {
                log.error("Error parsing genres JSON: {}", e.getMessage());
            }
        }

        String dbDirectors = rs.getString("directors");
        if (dbDirectors != null && !dbDirectors.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Director> filmDirectors = new TreeSet<>(Comparator.comparing(Director::getId));
                filmDirectors.addAll(objectMapper.readValue(dbDirectors, new TypeReference<Set<Director>>() {}));
                film.setDirectors(filmDirectors);
            } catch (JsonProcessingException e) {
                log.error("Error parsing directors JSON: {}", e.getMessage());
            }
        }

        return film;
    }

    private Set<Long> makeLongSet(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) return new HashSet<>();
        Object[] objectArray = (Object[]) sqlArray.getArray();
        return Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toSet());
    }
}
