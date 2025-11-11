package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.entity.ShoppingCart;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final WarehouseClient warehouseClient;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartMapper mapper;

    @Override
    public ShoppingCartDto getCart(String username) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> ShoppingCart.builder()
                        .username(username)
                        .isActive(true)
                        .build());
        log.info("get ShoppingCart {}", shoppingCart);
        return mapper.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Integer> products) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> ShoppingCart.builder()
                        .username(username)
                        .isActive(true)
                        .build());

        products.forEach((prodId, quantity) -> shoppingCart.getProducts().put(prodId, quantity));

        warehouseClient.checkProducts(mapper.toDto(shoppingCart));

        shoppingCartRepository.save(shoppingCart);
        log.info("add products to ShoppingCart {}", shoppingCart);
        return mapper.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public void deactivateCartByUsername(String username) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username).orElseThrow();
        shoppingCart.setIsActive(Boolean.FALSE);
        shoppingCartRepository.save(shoppingCart);
        log.info("deactivate ShoppingCart {}", username);
    }

    @Override
    @Transactional
    public ShoppingCartDto deleteProduct(String username, List<UUID> productIds) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username).orElseThrow();

        Map<UUID, Integer> products = shoppingCart.getProducts();
        for (UUID id : productIds) {
            Integer remove = products.remove(id);
            if (remove == 0) {
                throw new NoProductsInShoppingCartException(400, "No searched products in cart");
            }
        }
        shoppingCartRepository.save(shoppingCart);
        log.info("delete ShoppingCart {}", shoppingCart);
        return mapper.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username).orElseThrow();

        if (!shoppingCart.getIsActive()) {
            throw new IllegalArgumentException("Cart is not active");
        }

        Map<UUID, Integer> products = shoppingCart.getProducts();
        products.put(request.getProductId(), request.getNewQuantity());

        warehouseClient.checkProducts(mapper.toDto(shoppingCart));

        shoppingCartRepository.save(shoppingCart);
        log.info("change quantity ShoppingCart {}", shoppingCart);
        return mapper.toDto(shoppingCart);
    }
}