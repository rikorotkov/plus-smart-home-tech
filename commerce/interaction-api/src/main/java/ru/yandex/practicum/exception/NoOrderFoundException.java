package ru.yandex.practicum.exception;

import feign.FeignException;

public class NoOrderFoundException extends FeignException {
    public NoOrderFoundException(int status, String message) {
        super(status, message);
    }
}
