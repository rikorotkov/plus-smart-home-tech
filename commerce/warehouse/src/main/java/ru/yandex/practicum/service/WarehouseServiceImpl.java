package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.entity.WarehouseProduct;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final ShoppingStoreClient shoppingStoreClient;
    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseProductMapper mapper;

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsById(request.getProductId())) {
            throw new NoSpecifiedProductInWarehouseException(400, "Product id=" + request.getProductId() + "has already booked");
        }
        WarehouseProduct saved = warehouseProductRepository.save(mapper.toEntity(request));
        try {
            shoppingStoreClient.updateQuantityState(SetProductQuantityStateRequest.builder()
                    .productId(saved.getProductId())
                    .quantityState(QuantityState.ENDED)
                    .build());
        } catch (FeignException e) {
            log.info("Catching error trying to change QuantityState on product id={}", saved.getProductId());
        }
    }

    @Override
    public BookedProductsDto checkWarehouse(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Integer> cartProducts = shoppingCartDto.getProducts();
        Map<UUID, WarehouseProduct> products = warehouseProductRepository.findAllById(cartProducts.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        if (products.size() != cartProducts.size()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(400, "Some product from cart are unavailable");
        }
        double weight = 0;
        double volume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Integer> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct product = products.get(cartProduct.getKey());
            if (cartProduct.getValue() > product.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        400, "Not enough product id=" + cartProduct.getKey() + "in warehouse");
            }
            weight += product.getWeight() * cartProduct.getValue();
            volume += product.getHeight() * product.getWeight() * product.getDepth() * cartProduct.getValue();
            fragile = fragile || product.getFragile();
        }

        return BookedProductsDto.builder()
                .deliveryWeight(weight)
                .deliveryVolume(volume)
                .fragile(fragile)
                .build();
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct warehouseProduct = warehouseProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(400, "No info about product id=" + request.getProductId()));

        warehouseProduct.setQuantity(request.getQuantity());
        WarehouseProduct saved = warehouseProductRepository.save(warehouseProduct);
        Integer quantity = saved.getQuantity();
        try {
            shoppingStoreClient.updateQuantityState(SetProductQuantityStateRequest.builder()
                    .productId(saved.getProductId())
                    .quantityState(quantity == 0 ? QuantityState.ENDED : quantity < 10 ? QuantityState.FEW :
                            quantity <= 100 ? QuantityState.ENOUGH : QuantityState.MANY)
                    .build());
        } catch (FeignException e) {
            log.info("Catching error trying to change QuantityState on product id={}", saved.getProductId());
        }
    }

    @Override
    public AddressDto getAddress() {
        String currentAddress = AddressDto.getCurrentAddress();

        return AddressDto.builder()
                .country(currentAddress)
                .city(currentAddress)
                .street(currentAddress)
                .house(currentAddress)
                .flat(currentAddress)
                .build();
    }
}