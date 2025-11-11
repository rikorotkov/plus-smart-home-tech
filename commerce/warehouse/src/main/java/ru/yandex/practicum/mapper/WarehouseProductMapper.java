package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.entity.WarehouseProduct;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {
    @Mapping(target = "width", source = "dimension.width")
    @Mapping(target = "height", source = "dimension.height")
    @Mapping(target = "depth", source = "dimension.depth")
    @Mapping(target = "quantity", ignore = true)
    WarehouseProduct toEntity(NewProductInWarehouseRequest dto);
}