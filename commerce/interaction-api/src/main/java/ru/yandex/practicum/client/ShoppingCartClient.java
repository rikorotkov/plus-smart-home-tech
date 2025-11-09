package ru.yandex.practicum.client;

import feign.FeignException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "api/v1/shopping-cart")
public interface ShoppingCartClient {
    @GetMapping
    ShoppingCartDto getCart(@RequestParam @NotBlank String username) throws FeignException;

    @PutMapping
    ShoppingCartDto addProductsToCart(@RequestParam @NotBlank String username,
                                      @RequestBody Map<@NotNull UUID, @Positive Integer> products) throws FeignException;

    @DeleteMapping
    void deactivateCart(@RequestParam @NotBlank String username) throws FeignException;

    @PostMapping("/remove")
    ShoppingCartDto removeProductsFromCart(@RequestParam @NotBlank String username,
                                           @RequestBody List<@NotNull UUID> productsIds) throws FeignException;

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam @NotBlank String username,
                                          @RequestBody ChangeProductQuantityRequest request) throws FeignException;
}
