ALTER TABLE inventory
    ALTER COLUMN product_id SET NOT NULL,
    ALTER COLUMN quantity SET NOT NULL,
    ALTER COLUMN quantity SET DEFAULT 0,
    ADD CONSTRAINT uq_inventory_product_id UNIQUE (product_id),
    ADD CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_inventory_quantity_nonnegative CHECK (quantity >= 0);
