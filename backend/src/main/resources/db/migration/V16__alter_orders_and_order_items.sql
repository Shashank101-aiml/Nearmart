ALTER TABLE orders
    ALTER COLUMN customer_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ADD COLUMN created_at TIMESTAMP NOT NULL,
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE order_items
    RENAME COLUMN count TO quantity;

ALTER TABLE order_items
    ALTER COLUMN order_id SET NOT NULL,
    ALTER COLUMN quantity SET NOT NULL,
    ADD COLUMN product_title VARCHAR(255) NOT NULL,
    ADD COLUMN unit_price DOUBLE PRECISION NOT NULL,
    ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0);
