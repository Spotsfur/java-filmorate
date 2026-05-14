DELETE FROM film_genres;
DELETE FROM favorites;
DELETE FROM friends;

DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM ratings;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN id RESTART WITH 1;
ALTER TABLE ratings ALTER COLUMN id RESTART WITH 1;

INSERT INTO genres (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

INSERT INTO ratings (id, name) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');
