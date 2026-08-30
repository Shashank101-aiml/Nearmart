CREATE TABLE vendors (
    id BIGINT PRIMARY KEY REFERENCES users(id),
    store_name VARCHAR(255) NOT NULL,
    location VARCHAR(500) NOT NULL
);
