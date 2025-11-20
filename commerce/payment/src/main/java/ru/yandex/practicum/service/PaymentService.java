package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto orderDto);

    BigDecimal getTotalCost(OrderDto orderDto);

    PaymentDto successPay(UUID payId);

    BigDecimal getProductCost(OrderDto orderDto);

    PaymentDto failedPay(UUID payId);
}
