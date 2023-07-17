-- liquibase formatted sql

-- changeset northin:3

CREATE TABLE comments
(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    text       TEXT,
    user_id    INTEGER REFERENCES users (id)
);

-- liquibase formatted sql

-- changeset northin:4

ALTER TABLE comments
    ADD COLUMN ads_id INTEGER REFERENCES ads (id);
