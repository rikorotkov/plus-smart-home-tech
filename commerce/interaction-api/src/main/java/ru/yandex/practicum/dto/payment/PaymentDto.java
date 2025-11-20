package ru.yandex.practicum.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentDto {
    private UUID paymentId;

    private BigDecimal totalPayment;

    private BigDecimal deliveryTotal;

    private BigDecimal feeTotal;

    private PaymentStatus status;
}
