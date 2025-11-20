CREATE TABLE IF NOT EXISTS payment (
    payment_id UUID PRIMARY KEY,
    order_id UUID,
    product_cost NUMERIC(10,2),
    delivery_cost NUMERIC(10,2),
    total_cost NUMERIC(10,2),
    status VARCHAR(64)
);