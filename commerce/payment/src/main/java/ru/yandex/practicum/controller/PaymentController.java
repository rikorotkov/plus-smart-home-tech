package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController implements PaymentClient {
    private final PaymentService paymentService;

    @Override
    @PostMapping
    public PaymentDto createPayment(OrderDto orderDto) {
        return paymentService.createPayment(orderDto);
    }

    @Override
    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    @PostMapping("/refund")
    public PaymentDto successPay(UUID payId) {
        return paymentService.successPay(payId);
    }

    @Override
    @PostMapping("/productCost")
    public BigDecimal getProductCost(OrderDto orderDto) {
        return paymentService.getProductCost(orderDto);
    }

    @Override
    @PostMapping("/failed")
    public PaymentDto failedPay(UUID payId) {
        return paymentService.failedPay(payId);
    }
}
