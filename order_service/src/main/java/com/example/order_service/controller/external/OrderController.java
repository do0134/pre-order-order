package com.example.order_service.controller.external;

import com.example.order_service.utils.Response;
import com.example.order_service.model.dto.Order;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{userId}/{itemId}")
    public Response<Order> createOrder(@PathVariable("userId") Long userId, @PathVariable("itemId") Long itemId) {
        Order order = orderService.createOrderCache(userId, itemId);
        return Response.success(order);
    }

    @GetMapping("/user/{userId}")
    public Response<List<Order>> getUserOrder(@PathVariable("userId") Long userId) {
        List<Order> orderList = orderService.getUserOrder(userId);
        return Response.success(orderList);
    }

    @GetMapping("/{orderId}")
    public Response<Order> getOrder(@PathVariable("orderId") Long orderId) {
        Order order = orderService.getOrder(orderId);
        return Response.success(order);
    }

    @PostMapping("/pay/{userId}/{itemId}")
    public Response<Order> doPay(@PathVariable("userId") Long userId, @PathVariable("itemId") Long itemId) {
        Order order = orderService.pay(userId, itemId);
        return Response.success(order);
    }
}
