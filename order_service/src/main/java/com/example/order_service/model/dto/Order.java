package com.example.order_service.model.dto;

import lombok.Data;

@Data
public class Order {
    private OrderUser orderUser;
    private OrderItem orderItem;
    private Long quantity;
    private Long totalPrice;

    public static Order toDto(OrderUser orderUser, OrderItem orderItem, Long quantity, Long totalPrice) {
        Order order = new Order();
        order.setOrderUser(orderUser);
        order.setOrderItem(orderItem);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        return order;
    }
}
