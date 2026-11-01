CREATE TABLE projects (
    id          UUID PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);
