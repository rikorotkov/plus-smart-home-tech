package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.entity.Delivery;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    Delivery toEntity(DeliveryDto deliveryDto);

    DeliveryDto toDto(Delivery delivery);
}
