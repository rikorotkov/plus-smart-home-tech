package ru.yandex.practicum.service;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void addNewProductToWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto checkWarehouse(ShoppingCartDto shoppingCartDto);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getAddress();

    void shipProducts(ShippedToDeliveryRequest request);

    void returnProducts(Map<UUID, Integer> products);

    BookedProductsDto assembleOrder(@RequestBody AssemblyProductsForOrderRequest request);
}