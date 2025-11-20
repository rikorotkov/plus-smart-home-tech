package ru.yandex.practicum.dto.order;

import lombok.Data;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;

@Data
public class CreateNewOrderRequest {
    private ShoppingCartDto shoppingCart;

    private AddressDto deliveryAddress;
}
