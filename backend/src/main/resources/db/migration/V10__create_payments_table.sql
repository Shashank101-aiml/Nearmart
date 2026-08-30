CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    amount DOUBLE PRECISION,
    status VARCHAR(255)
);
