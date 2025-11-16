package ru.yandex.practicum.client;

import feign.FeignException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.UUID;

@FeignClient(name = "order-service", path = "api/v1/order")
public interface OrderClient {
    @GetMapping
    Page<OrderDto> getOrders(@RequestParam @NotBlank String username, Pageable pageable) throws FeignException;

    @PutMapping
    OrderDto createOrder(@RequestBody CreateNewOrderRequest request) throws FeignException;

    @PostMapping("/return")
    OrderDto returnOrder(@RequestBody ProductReturnRequest request) throws FeignException;

    @PostMapping("/payment")
    OrderDto payOrder(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/payment/failed")
    OrderDto failOrderPayment(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/delivery")
    OrderDto deliverOrder(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/delivery/failed")
    OrderDto failedDeliverOrder(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/completed")
    OrderDto completeOrder(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/calculate/total")
    OrderDto calculateTotal(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/calculate/delivery")
    OrderDto calculateDelivery(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/assembly")
    OrderDto assembleOrder(@RequestBody UUID orderId) throws FeignException;

    @PostMapping("/assembly/failed")
    OrderDto assembleOrderFailed(@RequestBody UUID orderId) throws FeignException;
}
