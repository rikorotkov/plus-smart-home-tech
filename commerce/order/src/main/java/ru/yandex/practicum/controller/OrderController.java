package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderService orderService;

    @Override
    @GetMapping
    public Page<OrderDto> getOrders(@RequestParam String username, Pageable pageable) {
        return orderService.getOrders(username, pageable);
    }

    @Override
    @PutMapping
    public OrderDto createOrder(@RequestBody CreateNewOrderRequest request) {
        return orderService.createOrder(request);
    }

    @Override
    @PostMapping("/return")
    public OrderDto returnOrder(@RequestBody ProductReturnRequest request) {
        return orderService.returnOrder(request);
    }

    @Override
    @PostMapping("/payment")
    public OrderDto payOrder(@RequestBody UUID orderId) {
        return orderService.payOrder(orderId);
    }

    @Override
    @PostMapping("/payment/failed")
    public OrderDto failOrderPayment(@RequestBody UUID orderId) {
        return orderService.failOrderPayment(orderId);
    }

    @Override
    @PostMapping("/delivery")
    public OrderDto deliverOrder(@RequestBody UUID orderId) {
        return orderService.deliverOrder(orderId);
    }

    @Override
    @PostMapping("/delivery/failed")
    public OrderDto failedDeliverOrder(@RequestBody UUID orderId) {
        return orderService.failedDeliverOrder(orderId);
    }

    @Override
    @PostMapping("/completed")
    public OrderDto completeOrder(@RequestBody UUID orderId) {
        return orderService.completeOrder(orderId);
    }

    @Override
    @PostMapping("/calculate/total")
    public OrderDto calculateTotal(@RequestBody UUID orderId) {
        return orderService.calculateTotal(orderId);
    }

    @Override
    @PostMapping("/calculate/delivery")
    public OrderDto calculateDelivery(@RequestBody UUID orderId) {
        return orderService.calculateDelivery(orderId);
    }

    @Override
    @PostMapping("/assembly")
    public OrderDto assembleOrder(@RequestBody UUID orderId) {
        return orderService.assembleOrder(orderId);
    }

    @Override
    @PostMapping("/assembly/failed")
    public OrderDto assembleOrderFailed(@RequestBody UUID orderId) {
        return orderService.assembleOrderFailed(orderId);
    }
}
