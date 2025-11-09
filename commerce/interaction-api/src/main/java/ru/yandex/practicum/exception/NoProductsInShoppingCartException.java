package ru.yandex.practicum.exception;

import feign.FeignException;

public class NoProductsInShoppingCartException extends FeignException {
    public NoProductsInShoppingCartException(int status, String message) {
        super(status, message);
    }
}