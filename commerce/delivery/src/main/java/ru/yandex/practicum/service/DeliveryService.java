package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {
    DeliveryDto addDelivery(DeliveryDto deliveryDto);

    void completeDelivery(UUID deliveryId);

    void pickDelivery(UUID deliveryId);

    void failDelivery(UUID deliveryId);

    BigDecimal calculateDelivery(DeliveryDto deliveryDto);
}
