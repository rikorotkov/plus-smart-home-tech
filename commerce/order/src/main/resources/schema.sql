CREATE TABLE IF NOT EXISTS orders (
    order_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cart_id UUID NOT NULL,
    payment_id UUID,
    delivery_id UUID,
    state varchar(20) NOT NULL,
    delivery_weight NUMERIC(10,2),
    delivery_volume NUMERIC(10,2),
    fragile BOOLEAN,
    total_price NUMERIC(10,2),
    delivery_price NUMERIC(10,2),
    product_price NUMERIC(10,2)
    );

CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT,
    CONSTRAINT order_products_pk PRIMARY KEY (order_id, product_id),
    CONSTRAINT order_products_cart_fk FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);