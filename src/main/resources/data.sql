-- Добавление рейтингов MPA
INSERT INTO mpa_ratings (id, rating_name) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17')
ON CONFLICT (id) DO UPDATE SET rating_name = EXCLUDED.rating_name;

-- Добавление жанров
INSERT INTO genres (id, full_name) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик')
ON CONFLICT (id) DO UPDATE SET full_name = EXCLUDED.full_name;