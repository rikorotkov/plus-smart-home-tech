package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.entity.Delivery;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    @Value("${delivery.base-price}")
    private BigDecimal DELIVERY_BASE;
    @Value("${delivery.fragile-k}")
    private BigDecimal FRAGILE_K;
    @Value("${delivery.weight-k}")
    private Double WEIGHT_K;
    @Value("${delivery.volume-k}")
    private Double VOLUME_K;
    @Value("${delivery.address-k}")
    private Double ADDRESS_K;
    @Value("${delivery.warehouse-address.address1}")
    private Double ADDRESS_1_K;
    @Value("${delivery.warehouse-address.address2}")
    private Double ADDRESS_2_K;

    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Override
    public DeliveryDto addDelivery(DeliveryDto deliveryDto) {
        Delivery entity = deliveryMapper.toEntity(deliveryDto);
        entity.setDeliveryState(DeliveryState.CREATED);
        Delivery saved = deliveryRepository.save(entity);

        log.info("Delivery added: {}", saved);
        return deliveryMapper.toDto(saved);
    }

    @Override
    public void completeDelivery(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(404, "Delivery id=" + orderId + "not found"));
        delivery.setDeliveryState(DeliveryState.DELIVERED);

        orderClient.deliverOrder(orderId);

        deliveryRepository.save(delivery);
    }

    @Override
    public void pickDelivery(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(404, "Delivery id=" + orderId + "not found"));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        orderClient.deliverOrder(orderId);
        warehouseClient.shipProductsToDelivery(ShippedToDeliveryRequest.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(orderId)
                .build());

        log.info("Delivery piked: {}", delivery);
        deliveryRepository.save(delivery);
    }

    @Override
    public void failDelivery(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(404, "Delivery id=" + orderId + "not found"));
        delivery.setDeliveryState(DeliveryState.FAILED);

        orderClient.failedDeliverOrder(orderId);

        log.info("Delivery failed: {}", delivery);
        deliveryRepository.save(delivery);
    }

    @Override
    public BigDecimal calculateDelivery(DeliveryDto dto) {

        BigDecimal price = DELIVERY_BASE;

        String warehouseAddress = dto.getFromAddress().getCity();
        BigDecimal warehouseK;

        if (warehouseAddress.equals("ADDRESS_1")) {
            warehouseK = BigDecimal.valueOf(ADDRESS_1_K);
        } else if (warehouseAddress.equals("ADDRESS_2")) {
            warehouseK = BigDecimal.valueOf(ADDRESS_2_K);
        } else {
            throw new RuntimeException("Wrong warehouse address");
        }

        price = price.add(price.multiply(warehouseK));

        if (dto.isFragile()) {
            price = price.add(price.multiply(FRAGILE_K));
        }

        BigDecimal weightComponent = BigDecimal.valueOf(dto.getDeliveryWeight())
                .multiply(BigDecimal.valueOf(WEIGHT_K));

        price = price.add(weightComponent);

        BigDecimal volumeComponent = BigDecimal.valueOf(dto.getDeliveryVolume())
                .multiply(BigDecimal.valueOf(VOLUME_K));

        price = price.add(volumeComponent);

        BigDecimal addressK = dto.getFromAddress().getStreet()
                .equals(dto.getToAddress().getStreet())
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ADDRESS_K);

        price = price.add(price.multiply(addressK));

        BigDecimal result = price.setScale(2, BigDecimal.ROUND_HALF_UP);

        log.info("Delivery calculated: {}", result);
        return result;
    }
}
