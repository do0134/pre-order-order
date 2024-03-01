package com.example.order_service.service;

import com.example.order_service.model.dto.Order;
import com.example.order_service.model.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrderCache(Long userId, Long itemId);
    List<Order> getUserOrder(Long userId);
    Order getOrder(Long orderId);
    OrderResponse pay(Long userId, Long itemId);
}
