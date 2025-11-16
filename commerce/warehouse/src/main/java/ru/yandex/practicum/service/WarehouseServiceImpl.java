package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.entity.Booking;
import ru.yandex.practicum.entity.WarehouseProduct;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final ShoppingStoreClient shoppingStoreClient;
    private final WarehouseProductRepository warehouseProductRepository;
    private final BookingRepository bookingRepository;
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

    @Override
    public BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest request) {
        Map<UUID, Integer> orderProducts = request.getProducts();
        Map<UUID, WarehouseProduct> products = warehouseProductRepository.findAllById(orderProducts.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        if (products.size() != orderProducts.size()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(400, "Some product are not available");
        }

        double weight = 0;
        double volume = 0;
        boolean fragile = false;
        for (Map.Entry<UUID, Integer> cartProduct : orderProducts.entrySet()) {
            WarehouseProduct product = products.get(cartProduct.getKey());
            int newQuantity = product.getQuantity() - cartProduct.getValue();
            if (newQuantity < 0) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(404, "Some products are not enough");
            }
            product.setQuantity(newQuantity);
            weight += product.getWeight() * cartProduct.getValue();
            volume += product.getHeight() * product.getWeight() * product.getDepth() * cartProduct.getValue();
            fragile = fragile || product.getFragile();
        }
        Booking booking = Booking.builder()
                .orderId(request.getOrderId())
                .products(request.getProducts())
                .build();
        bookingRepository.save(booking);
        warehouseProductRepository.saveAll(products.values());

        return BookedProductsDto.builder()
                .fragile(fragile)
                .deliveryVolume(volume)
                .deliveryWeight(weight)
                .build();
    }

    @Override
    public void shipProducts(ShippedToDeliveryRequest request) {
        Booking booking = bookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ProductNotFoundException(404, "Can't ship order=" + request.getOrderId()));

        booking.setDeliveryId(request.getDeliveryId());
        bookingRepository.save(booking);

    }

    @Override
    public void returnProducts(Map<UUID, Integer> products) {
        products.forEach((key, value) -> {
            WarehouseProduct product = warehouseProductRepository.findById(key)
                    .orElseThrow(() -> new ProductNotFoundException(400, "No info about product id=" + key));
            product.setQuantity(product.getQuantity() + value);
            warehouseProductRepository.save(product);
        });
    }
}