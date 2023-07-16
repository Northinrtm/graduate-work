-- liquibase formatted sql

-- changeset northin:3

CREATE TABLE comments (
                          id SERIAL PRIMARY KEY,
                          created_at TIMESTAMP,
                          text TEXT,
                          user_id INTEGER REFERENCES users(id)
);