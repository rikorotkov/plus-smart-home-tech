package ru.yandex.practicum.exception;

import feign.FeignException;

public class SpecifiedProductAlreadyInWarehouseException extends FeignException {
    protected SpecifiedProductAlreadyInWarehouseException(int status, String message) {
        super(status, message);
    }
}