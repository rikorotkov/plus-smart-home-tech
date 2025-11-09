package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "shopping_cart_id", nullable = false)
    private UUID shoppingCartId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "products_shopping_cart",
            joinColumns = @JoinColumn(name = "shopping_cart_id"))
    @MapKeyColumn(name = "product_id")
    @Builder.Default
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();
}
