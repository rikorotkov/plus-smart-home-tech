package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.UUID;

public interface OrderService {
    Page<OrderDto> getOrders(String username, Pageable pageable);

    OrderDto createOrder(CreateNewOrderRequest request);

    OrderDto returnOrder(ProductReturnRequest request);

    OrderDto payOrder(UUID orderId);

    OrderDto failOrderPayment(UUID orderId);

    OrderDto deliverOrder(UUID orderId);

    OrderDto failedDeliverOrder(UUID orderId);

    OrderDto completeOrder(UUID orderId);

    OrderDto calculateTotal(UUID orderId);

    OrderDto calculateDelivery(UUID orderId);

    OrderDto assembleOrder(UUID orderId);

    OrderDto assembleOrderFailed(UUID orderId);
}
