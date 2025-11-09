package ru.yandex.practicum.exception;

import feign.FeignException;

public class ProductInShoppingCartLowQuantityInWarehouse extends FeignException {
    public ProductInShoppingCartLowQuantityInWarehouse(int status, String message) {
        super(status, message);
    }
}