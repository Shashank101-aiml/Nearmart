ALTER TABLE carts
    ALTER COLUMN customer_id SET NOT NULL,
    ADD CONSTRAINT uq_carts_customer_id UNIQUE (customer_id),
    ADD CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers(id);
