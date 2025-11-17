package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.entity.Order;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ShoppingCartClient shoppingCartClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final WarehouseClient warehouseClient;
    private final OrderRepository orderRepository;

    @Override
    public Page<OrderDto> getOrders(String username, Pageable pageable) {
        ShoppingCartDto cart = shoppingCartClient.getCart(username);
        UUID shoppingCartId = cart.getShoppingCartId();
        Page<Order> ordersByShoppingCartId = orderRepository.getOrdersByShoppingCartId(shoppingCartId, pageable);
        return ordersByShoppingCartId.map(orderMapper::toDto);
    }

    @Override
    public OrderDto createOrder(CreateNewOrderRequest request) {
        BookedProductsDto bookedProductsDto = warehouseClient.checkProducts(request.getShoppingCart());
        Order newOrder = Order.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedProductsDto.getDeliveryWeight())
                .deliveryVolume(bookedProductsDto.getDeliveryVolume())
                .fragile(bookedProductsDto.getFragile())
                .build();

        Order saved = orderRepository.save(newOrder);

        PaymentDto payment = paymentClient.createPayment(orderMapper.toDto(saved));
        DeliveryDto deliveryDto = deliveryClient.addDelivery(DeliveryDto.builder()
                .fromAddress(warehouseClient.getAddress())
                .toAddress(request.getDeliveryAddress())
                .orderId(saved.getOrderId())
                .deliveryState(DeliveryState.CREATED)
                .deliveryWeight(saved.getDeliveryWeight())
                .deliveryVolume(saved.getDeliveryVolume())
                .fragile(saved.isFragile())
                .build());

        saved.setDeliveryId(deliveryDto.getDeliveryId());
        saved.setPaymentId(payment.getPaymentId());
        saved.setState(OrderState.ON_PAYMENT);

        orderRepository.save(saved);

        log.info("Order created: {}", saved);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));

        deliveryClient.failDelivery(request.getOrderId());
        warehouseClient.returnProducts(request.getProducts());

        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);

        log.info("Order returned: {}", order.getOrderId());
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto payOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        PaymentDto paymentDto = paymentClient.successPay(order.getPaymentId());

        order.setState(OrderState.PAID);
        Order saved = orderRepository.save(order);

        log.info("Order payed: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto failOrderPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        PaymentDto paymentDto = paymentClient.failedPay(order.getPaymentId());

        order.setState(OrderState.PAYMENT_FAILED);
        Order saved = orderRepository.save(order);

        log.info("Order failed: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto deliverOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        deliveryClient.completeDelivery(orderId);

        order.setState(OrderState.DELIVERED);
        Order saved = orderRepository.save(order);

        log.info("Order delivered: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto failedDeliverOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        deliveryClient.failDelivery(orderId);

        order.setState(OrderState.DELIVERY_FAILED);
        Order saved = orderRepository.save(order);

        log.info("Order failed: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto completeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));

        order.setState(OrderState.COMPLETED);
        Order saved = orderRepository.save(order);

        log.info("Order completed: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));

        BigDecimal totalCost = paymentClient.getTotalCost(orderMapper.toDto(order));

        order.setTotalPrice(totalCost);
        Order saved = orderRepository.save(order);

        log.info("Order calculated: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));

        BigDecimal totalCost = deliveryClient.calculateDelivery(DeliveryDto.builder()
                .deliveryId(order.getDeliveryId())
                .build());

        order.setTotalPrice(totalCost);
        Order saved = orderRepository.save(order);

        log.info("Order delivery calculated: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto assembleOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        warehouseClient.assembleOrder(AssemblyProductsForOrderRequest.builder()
                .orderId(orderId)
                .products(order.getProducts())
                .build());

        order.setState(OrderState.ASSEMBLED);
        Order saved = orderRepository.save(order);

        log.info("Order assembled: {}", orderId);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderDto assembleOrderFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(404, "Order not found"));
        order.setState(OrderState.ASSEMBLY_FAILED);
        Order saved = orderRepository.save(order);


        log.info("Order failed assembled: {}", orderId);
        return orderMapper.toDto(saved);
    }
}
