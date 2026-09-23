CREATE TABLE delivery_partners (
    id BIGINT PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    vehicle_number VARCHAR(50),
    available BOOLEAN NOT NULL DEFAULT TRUE
);
