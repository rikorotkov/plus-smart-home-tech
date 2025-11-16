package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentStatus;
import ru.yandex.practicum.entity.Payment;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Value("${tax}")
    private BigDecimal TAX_K;

    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .productCost(getProductCost(orderDto))
                .deliveryCost(orderDto.getDeliveryPrice())
                .totalCost(getTotalCost(orderDto))
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: {}", saved);
        return paymentMapper.toDto(saved);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {

        BigDecimal productCost =
                orderDto.getProductPrice() == null
                        ? getProductCost(orderDto)
                        : orderDto.getProductPrice();

        BigDecimal deliveryPrice = orderDto.getDeliveryPrice();

        log.info("Product cost: {}", productCost);

        BigDecimal taxAmount = productCost.multiply(TAX_K);

        return productCost.add(deliveryPrice).add(taxAmount);
    }

    @Override
    public BigDecimal getProductCost(OrderDto orderDto) {
        return orderDto.getProducts().entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    BigDecimal price =
                            shoppingStoreClient.getProductById(productId).getPrice();
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public PaymentDto successPay(UUID payId) {
        Payment payment = paymentRepository.findById(payId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        orderClient.payOrder(payment.getOrderId());
        log.info("Payment successful: {}", payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto failedPay(UUID payId) {
        Payment payment = paymentRepository.findById(payId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        orderClient.failOrderPayment(payment.getOrderId());
        log.info("Payment failed: {}", payment);
        return paymentMapper.toDto(payment);
    }
}
