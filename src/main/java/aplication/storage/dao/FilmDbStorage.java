package aplication.storage.dao;

import aplication.exception.NotFoundException;
import aplication.model.Film;
import aplication.model.Genre;
import aplication.model.Mpa;
import aplication.storage.FilmStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.springframework.util.ObjectUtils.toObjectArray;

@Primary
@Component
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getDouble("duration"));

        // Получение рейтинга MPA
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("rating_name")); // Поле rating_name должно быть в SQL-запросе
        film.setMpa(mpa);

        // Получение жанров для фильма
        film.setGenres(new HashSet<>(getFilmGenres(film.getId())));

        return film;
    };

    @Override
    public Film add(Film film) {
        verifyMpaExist(film);

        var genres = film.getGenres().stream()
                .mapToInt(Genre::getId)
                .toArray();

        var filmId = addFilmAndGetId(film);
        film = getById(filmId);

        if (genres.length > 0) {
            verifyGenresExist(genres);
            addGenres(film);
        }

        return getById(filmId);
    }

    @Override
    public Film update(Film film) {
        // Обновляем данные фильма
        String sql = "UPDATE films SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());

        // Обновляем жанры
        updateGenres(film);

        return film;
    }

    @Override
    public void delete(long filmId) {
        // Удаляем фильм, жанры будут удалены автоматически благодаря ON DELETE CASCADE
        String sql = "DELETE FROM films WHERE id = ?";
        jdbc.update(sql, filmId);
    }

    @Override
    public Film addLike(long filmId, long userId) {
        // SQL-запрос для добавления лайка
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        // Выполняем вставку лайка
        jdbc.update(sql, filmId, userId);

        // Возвращаем обновлённый объект фильма
        return getById(filmId); // Метод getById загружает фильм из базы данных вместе с лайками и жанрами
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        // SQL-запрос для удаления лайка
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        // Выполняем удаление лайка
        jdbc.update(sql, filmId, userId);

        // Возвращаем обновлённый объект фильма
        return getById(filmId); // Метод getById загружает фильм из базы данных вместе с лайками и жанрами
    }

    @Override
    public Film getById(long filmId) {
        // SQL-запрос с большим JOIN для получения фильма, его жанров и лайков
        String sql = "SELECT f.id, f.film_name, f.description, f.release_date, f.duration, " +
                "m.id AS mpa_id, m.rating_name, " +
                "g.id AS genre_id, g.full_name AS genre_name, " +
                "l.user_id AS like_user_id " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "WHERE f.id = ?";

        // Выполняем запрос и извлекаем данные
        return jdbc.query(sql, rs -> {
            Film film = null;
            Set<Genre> genres = new HashSet<>();
            Set<Long> likes = new HashSet<>();

            while (rs.next()) {
                // Создаём объект фильма, если он ещё не инициализирован
                if (film == null) {
                    film = new Film();
                    film.setId(rs.getLong("id"));
                    film.setName(rs.getString("film_name"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    film.setDuration(rs.getDouble("duration"));

                    // Устанавливаем MPA рейтинг
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("mpa_id"));
                    mpa.setName(rs.getString("rating_name"));
                    film.setMpa(mpa);
                }

                // Добавляем жанры
                int genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    Genre genre = new Genre();
                    genre.setId(genreId);
                    genre.setName(rs.getString("genre_name"));
                    genres.add(genre);
                }

                // Добавляем лайки
                long likeUserId = rs.getLong("like_user_id");
                if (!rs.wasNull()) {
                    likes.add(likeUserId);
                }
            }

            // Устанавливаем жанры и лайки в фильм
            if (film != null) {
                film.setGenres(genres);
                film.setLikes(likes);
            }

            return film;
        }, filmId);
    }

    @Override
    public List<Film> getAll() {
        // SQL-запрос с большим JOIN для загрузки всех данных
        String sql = "SELECT f.*, m.rating_name, g.id AS genre_id, g.full_name AS genre_name, l.user_id AS like_user_id " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id " +
                "LEFT JOIN likes l ON f.id = l.film_id";

        // Выполняем запрос
        return jdbc.query(sql, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                // Получаем фильм из Map или создаём новый, если его ещё нет
                long filmId = rs.getLong("id");
                Film film = filmMap.computeIfAbsent(filmId, id -> {
                    Film newFilm = new Film();
                    newFilm.setId(filmId);
                    try {
                        newFilm.setName(rs.getString("film_name"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        newFilm.setDescription(rs.getString("description"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        newFilm.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        newFilm.setDuration(rs.getDouble("duration"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Устанавливаем MPA
                    Mpa mpa = new Mpa();
                    try {
                        mpa.setId(rs.getInt("mpa_id"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        mpa.setName(rs.getString("rating_name"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    newFilm.setMpa(mpa);

                    // Инициализируем пустые наборы для жанров и лайков
                    newFilm.setGenres(new HashSet<>());
                    newFilm.setLikes(new HashSet<>());

                    return newFilm;
                });

                // Добавляем жанр, если он есть
                int genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    Genre genre = new Genre();
                    genre.setId(genreId);
                    genre.setName(rs.getString("genre_name"));
                    film.getGenres().add(genre);
                }

                // Добавляем лайк, если он есть
                long likeUserId = rs.getLong("like_user_id");
                if (!rs.wasNull()) {
                    film.getLikes().add(likeUserId);
                }
            }

            // Возвращаем фильмы как список
            return new ArrayList<>(filmMap.values());
        });
    }

    // Приватный метод для добавления жанров
    private void addGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(sql, film.getId(), genre.getId());
            }
        }
    }

    // Приватный метод для обновления жанров
    private void updateGenres(Film film) {
        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());

        // Добавляем новые жанры
        addGenres(film);
    }

    // Получение жанров для фильма
    private List<Genre> getFilmGenres(long filmId) {
        String genreSql = "SELECT g.id, g.full_name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbc.query(genreSql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("full_name"));
            return genre;
        }, filmId);
    }

    private long addFilmAndGetId(Film film) {
        // SQL-запрос с вставкой данных
        String sql = "INSERT INTO films (film_name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        // Создаём KeyHolder для хранения сгенерированного ключа
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"}); // Указываем столбец автоинкремента
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setDouble(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        // Получаем значение автоинкремента
        return keyHolder.getKey().longValue();
    }

    private void verifyGenresExist(int[] genreIds) {
        String sql = "SELECT id FROM genres WHERE id IN (%s)";
        String placeholders = String.join(",", java.util.Collections.nCopies(genreIds.length, "?"));
        sql = String.format(sql, placeholders);

        // Выполняем запрос, чтобы получить существующие жанры
        List<Integer> existingGenres = jdbc.queryForList(sql, Integer.class, toObjectArray(genreIds));

        // Проверяем каждый переданный ID
        for (int id : genreIds) {
            if (!existingGenres.contains(id)) {
                throw new NotFoundException("Жанр с id " + id + " не найден");
            }
        }
    }

    private void verifyMpaExist(Film film) {
        // Проверяем корректность MPA
        if (film.getMpa() == null || film.getMpa().getId() <= 0) {
            throw new NotFoundException("MPA рейтинг не может быть пустым или некорректным");
        }

        // Проверяем, существует ли MPA в базе данных
        String mpaCheckSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer mpaExists = jdbc.queryForObject(mpaCheckSql, Integer.class, film.getMpa().getId());
        if (mpaExists == null || mpaExists == 0) {
            throw new NotFoundException("MPA рейтинг с id " + film.getMpa().getId() + " не существует");
        }
    }


}