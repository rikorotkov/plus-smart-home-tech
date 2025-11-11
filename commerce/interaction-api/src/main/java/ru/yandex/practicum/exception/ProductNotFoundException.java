package ru.yandex.practicum.exception;

import feign.FeignException;

public class ProductNotFoundException extends FeignException {
    public ProductNotFoundException(int status, String message) {
        super(status, message);
    }
}