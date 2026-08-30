CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    driver_name VARCHAR(255),
    tracking_status VARCHAR(255)
);
