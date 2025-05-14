-- Подключение расширения pg_trgm
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL,
    CONSTRAINT users_unique_email UNIQUE (email),
    CONSTRAINT users_unique_login UNIQUE (login)
);

-- Индексы для пользователей
CREATE INDEX IF NOT EXISTS idx_users_login_trgm ON users USING gin(login gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING gin(username gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_email_trgm ON users USING gin(email gin_trgm_ops);

-- Таблица друзей
CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    friend_id BIGINT REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

-- Таблица рейтингов (ENUM вместо отдельной таблицы)
CREATE TABLE IF NOT EXISTS mpa_ratings (
    id SERIAL PRIMARY KEY,
    rating_name VARCHAR(50) NOT NULL,
    CONSTRAINT unique_rating_name UNIQUE (rating_name)
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id BIGSERIAL PRIMARY KEY,
    film_name VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration DECIMAL NOT NULL,
    mpa_id INT REFERENCES mpa_ratings (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

-- Индексы для фильмов
CREATE INDEX IF NOT EXISTS idx_films_name_trgm ON films USING gin(film_name gin_trgm_ops);

-- Таблица режиссёров
CREATE TABLE IF NOT EXISTS directors (
    id SERIAL PRIMARY KEY,
    director_name VARCHAR(50) NOT NULL
);

-- Таблица связи фильмов и режиссёров
CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT REFERENCES films (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    director_id INT REFERENCES directors (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT film_directors_pk PRIMARY KEY (film_id, director_id)
);

-- Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(50) NOT NULL UNIQUE
);

-- Индексы для жанров
CREATE INDEX IF NOT EXISTS idx_genres_name_trgm ON genres USING gin(full_name gin_trgm_ops);

-- Таблица связи фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    genre_id INT REFERENCES genres(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id)
);

-- Таблица лайков
CREATE TABLE IF NOT EXISTS likes (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT likes_pk PRIMARY KEY (user_id, film_id)
);

-- Перечисления событий
DO
' 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''event_type_enum'') THEN
        CREATE TYPE event_type_enum AS ENUM (''LIKE'', ''REVIEW'', ''FRIEND'');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''operation_type_enum'') THEN
        CREATE TYPE operation_type_enum AS ENUM (''ADD'', ''REMOVE'', ''UPDATE'');
    END IF;
END
';

-- Таблица событий
CREATE TABLE IF NOT EXISTS user_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type event_type_enum NOT NULL,
    operation operation_type_enum NOT NULL,
    entity_id BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content TEXT NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    useful INT DEFAULT 0
);

-- Таблица связи отзывов и пользователей
CREATE TABLE IF NOT EXISTS review_reactions (
    review_id BIGINT NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id)
);
