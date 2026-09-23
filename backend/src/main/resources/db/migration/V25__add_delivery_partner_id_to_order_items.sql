ALTER TABLE order_items ADD COLUMN delivery_partner_id BIGINT REFERENCES delivery_partners(id);
