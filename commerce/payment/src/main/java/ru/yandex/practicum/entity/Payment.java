package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    private UUID orderId;

    private BigDecimal productCost;

    private BigDecimal deliveryCost;

    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
