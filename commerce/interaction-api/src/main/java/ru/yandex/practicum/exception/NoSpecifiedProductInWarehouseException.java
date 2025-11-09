package ru.yandex.practicum.exception;

import feign.FeignException;

public class NoSpecifiedProductInWarehouseException extends FeignException {
    public NoSpecifiedProductInWarehouseException(int status, String message) {
        super(status, message);
    }
}