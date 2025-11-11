CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(127) NOT NULL,
    role VARCHAR(20) NOT NULL
);
