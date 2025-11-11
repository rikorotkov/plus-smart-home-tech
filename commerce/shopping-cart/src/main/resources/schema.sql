CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    is_active BOOL NOT NULL DEFAULT true
    );

CREATE TABLE IF NOT EXISTS products_shopping_cart (
    product_id UUID,
    shopping_cart_id UUID,
    quantity int NOT NULL CHECK(quantity > 0),
    CONSTRAINT products_shopping_cart_pk PRIMARY KEY(product_id, shopping_cart_id),
    CONSTRAINT products_shopping_cart_shopping_cart_fk
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_cart(shopping_cart_id) ON DELETE CASCADE
    );