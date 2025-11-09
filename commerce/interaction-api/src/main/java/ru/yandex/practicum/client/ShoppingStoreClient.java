package ru.yandex.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient {
    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable) throws FeignException;

    @PutMapping
    ProductDto addProduct(@RequestBody ProductDto productDto) throws FeignException;

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto) throws FeignException;

    @PostMapping("/removeProductFromStore")
    ProductDto deleteProduct(@RequestBody UUID id) throws FeignException;

    @PostMapping("/quantityState")
    ProductDto updateQuantityState(SetProductQuantityStateRequest request) throws FeignException;

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId) throws FeignException;
}
