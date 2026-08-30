ALTER TABLE products
    ADD COLUMN vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    ADD COLUMN description TEXT,
    ADD COLUMN available BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
