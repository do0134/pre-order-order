package com.example.order_service.model.dto.response;


import lombok.Data;

@Data
public class OrderResponse {
    private Long userId;
    private Long itemId;
    private Long quantity = 1L;
    private Long totalPrice = 1L;

    public static OrderResponse toDto(Long userId, Long itemId) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setUserId(userId);
        orderResponse.setItemId(itemId);
        return orderResponse;
    }
}
