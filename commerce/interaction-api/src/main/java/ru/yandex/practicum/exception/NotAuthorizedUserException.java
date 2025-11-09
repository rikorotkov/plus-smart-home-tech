package ru.yandex.practicum.exception;

import feign.FeignException;

public class NotAuthorizedUserException extends FeignException {
    public NotAuthorizedUserException(int status, String message) {
        super(status, message);
    }
}