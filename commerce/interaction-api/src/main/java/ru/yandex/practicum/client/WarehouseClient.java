package ru.yandex.practicum.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient {

    @PutMapping
    void addNewProductToWareHouse(@RequestBody NewProductInWarehouseRequest request) throws FeignException;

    @PostMapping("/check")
    BookedProductsDto checkProducts(@RequestBody ShoppingCartDto shoppingCartDto) throws FeignException;

    @PostMapping("/add")
    void addProductsToWareHouse(@RequestBody AddProductToWarehouseRequest request) throws FeignException;

    @GetMapping("/address")
    AddressDto getAddress() throws FeignException;

    @PostMapping("/shipped")
    void shipProductsToDelivery(@RequestBody ShippedToDeliveryRequest request) throws FeignException;

    @PostMapping("/return")
    void returnProducts(Map<UUID, Integer> products) throws FeignException;

    @PostMapping("/assembly")
    BookedProductsDto assembleOrder(@RequestBody AssemblyProductsForOrderRequest request) throws FeignException;
}