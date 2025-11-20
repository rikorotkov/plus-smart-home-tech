package ru.yandex.practicum.exception;

import feign.FeignException;

public class NoDeliveryFoundException extends FeignException {
    public NoDeliveryFoundException(int status, String message) {
        super(status, message);
    }
}
