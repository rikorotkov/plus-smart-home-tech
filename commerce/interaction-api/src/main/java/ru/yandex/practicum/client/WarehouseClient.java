package ru.yandex.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

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
}
