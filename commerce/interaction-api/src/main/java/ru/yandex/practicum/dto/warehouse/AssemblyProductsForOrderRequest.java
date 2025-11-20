package ru.yandex.practicum.dto.warehouse;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AssemblyProductsForOrderRequest {
    private Map<UUID, Integer> products;

    private UUID orderId;
}
