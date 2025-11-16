package ru.yandex.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery-service", path = "api/v1/delivery")
public interface DeliveryClient {
    @PutMapping
    DeliveryDto addDelivery(@RequestBody DeliveryDto deliveryDto) throws FeignException;

    @PostMapping("/successful")
    void completeDelivery(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/picked")
    void pickDelivery(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/failed")
    void failDelivery(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/cost")
    BigDecimal calculateDelivery(@RequestBody DeliveryDto deliveryDto) throws FeignException;
}
