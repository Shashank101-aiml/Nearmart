CREATE TABLE customers (
    id BIGINT PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL
);
