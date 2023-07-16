-- liquibase formatted sql

-- changeset northin:2

CREATE TABLE ads (
                     id SERIAL PRIMARY KEY,
                     price INTEGER,
                     title VARCHAR(255),
                     description TEXT,
                     user_id INTEGER REFERENCES users(id)
);