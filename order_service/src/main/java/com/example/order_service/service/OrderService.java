package com.example.order_service.service;

import com.example.order_service.model.dto.Order;

import java.util.List;

public interface OrderService {
    Order makeOrder(Long userId, Long itemId, Long quantity);
    List<Order> getUserOrder(Long userId);
    Order getOrder(Long orderId);
    void pay(Long userId, Long itemId);
}
