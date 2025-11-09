package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookedProductsDto {
    @NotNull
    private Double deliveryWeight;
    @NotNull
    private Double deliveryVolume;
    @NotNull
    private Boolean fragile;
}