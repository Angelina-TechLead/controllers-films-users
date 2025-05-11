CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL,
    CONSTRAINT users_unique_email UNIQUE (username),
    CONSTRAINT users_unique_email UNIQUE (email),
    CONSTRAINT users_unique_login UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    friend_id BIGINT REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS mpa_ratings (
    id SERIAL PRIMARY KEY,
    rating_name VARCHAR(50) NOT NULL,
    CONSTRAINT unique_rating_name UNIQUE (rating_name)
);

CREATE TABLE IF NOT EXISTS films (
    id BIGSERIAL PRIMARY KEY,
    film_name VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration DECIMAL NOT NULL,
    mpa_id INT REFERENCES mpa_ratings (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
);

CREATE TABLE IF NOT EXISTS directors (
    id SERIAL PRIMARY KEY,
    director_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT REFERENCES films (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    director_id INT REFERENCES directors (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT film_directors_pk PRIMARY KEY (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(50) NOT NULL,
    CONSTRAINT unique_full_name UNIQUE (full_name)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT REFERENCES films (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    genre_id INT REFERENCES genres (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS likes (
    user_id BIGINT REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    film_id BIGINT REFERENCES films (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT likes_pk PRIMARY KEY (user_id, film_id)
);
