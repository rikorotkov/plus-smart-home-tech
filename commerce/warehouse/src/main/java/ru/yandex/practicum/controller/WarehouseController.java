package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void addNewProductToWareHouse(@RequestBody NewProductInWarehouseRequest request) {
        warehouseService.addNewProductToWarehouse(request);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkProducts(@RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkWarehouse(shoppingCartDto);
    }

    @Override
    @PostMapping("/add")
    public void addProductsToWareHouse(@RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public AddressDto getAddress() {
        return warehouseService.getAddress();
    }

    @Override
    public void shipProductsToDelivery(ShippedToDeliveryRequest request) {
        warehouseService.shipProducts(request);
    }

    @Override
    public void returnProducts(Map<UUID, Integer> products) {
        warehouseService.returnProducts(products);
    }

    @Override
    public BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest request) {
        return warehouseService.assembleOrder(request);
    }
}