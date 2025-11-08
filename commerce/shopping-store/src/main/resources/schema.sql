CREATE TABLE IF NOT EXISTS product (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(500),
    quantity_state VARCHAR(15) NOT NULL,
    product_state VARCHAR(15) NOT NULL,
    product_category VARCHAR(15) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 1),
    CONSTRAINT chk_product_category CHECK (product_category in ('LIGHTING', 'CONTROL', 'SENSORS')),
    CONSTRAINT chk_product_state CHECK (product_state in ('ACTIVE', 'DEACTIVATE')),
    CONSTRAINT chk_quantity_state CHECK (quantity_state in ('ENDED', 'FEW', 'ENOUGH', 'MANY'))
);