package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    @Override
    @PutMapping
    public DeliveryDto addDelivery(@RequestBody DeliveryDto deliveryDto) {
        return deliveryService.addDelivery(deliveryDto);
    }

    @Override
    @PostMapping("/successful")
    public void completeDelivery(@RequestBody UUID orderId) {
        deliveryService.completeDelivery(orderId);
    }

    @Override
    @PostMapping("/picked")
    public void pickDelivery(@RequestBody UUID orderId) {
        deliveryService.pickDelivery(orderId);
    }

    @Override
    @PostMapping("/failed")
    public void failDelivery(@RequestBody UUID orderId) {
        deliveryService.failDelivery(orderId);
    }

    @Override
    @PostMapping("/cost")
    public BigDecimal calculateDelivery(@RequestBody DeliveryDto deliveryDto) {
        return deliveryService.calculateDelivery(deliveryDto);
    }
}
