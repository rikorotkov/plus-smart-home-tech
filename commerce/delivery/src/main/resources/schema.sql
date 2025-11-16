CREATE TABLE IF NOT EXISTS address (
    address_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    country varchar(20) NOT NULL,
    city varchar(50) NOT NULL,
    street varchar(50) NOT NULL,
    house varchar(20) NOT NULL,
    flat varchar(20)
    );

CREATE TABLE IF NOT EXISTS delivery (
    delivery_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id UUID NOT NULL,
    from_address_id UUID NOT NULL,
    to_address_id UUID NOT NULL,
    delivery_state varchar(20),
    delivery_weight NUMERIC(10,2),
    delivery_volume NUMERIC(10,2),
    fragile BOOLEAN,
    CONSTRAINT delivery_from_address_fk FOREIGN KEY (from_address_id) REFERENCES address(address_id) ON DELETE CASCADE,
    CONSTRAINT delivery_to_address_fk FOREIGN KEY (to_address_id) REFERENCES address(address_id) ON DELETE CASCADE
);