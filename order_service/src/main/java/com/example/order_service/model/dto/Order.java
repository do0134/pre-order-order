package com.example.order_service.model.dto;

import lombok.Data;

@Data
public class Order {
    private OrderUser orderUser;
    private OrderItem orderItem;
    private Long quantity = 1L;
    private Long totalPrice = 1L;

    public static Order toDto(OrderUser orderUser, OrderItem orderItem) {
        Order order = new Order();
        order.setOrderUser(orderUser);
        order.setOrderItem(orderItem);
        return order;
    }
}
