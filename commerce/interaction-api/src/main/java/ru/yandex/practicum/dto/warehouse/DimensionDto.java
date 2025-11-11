package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class DimensionDto {
    @DecimalMin("1.0")
    private Double width;

    @DecimalMin("1.0")
    private Double height;

    @DecimalMin("1.0")
    private Double depth;
}