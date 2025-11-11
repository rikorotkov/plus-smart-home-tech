package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.util.UUID;

@Data
public class NewProductInWarehouseRequest {
    private UUID productId;

    private Boolean fragile;

    private DimensionDto dimension;

    @DecimalMin("1.0")
    private Double weight;
}