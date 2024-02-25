package com.example.order_service.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsedStock {
    private Long userId;
    private Long salesItemId;

    public static UsedStock toDto(Long userId, Long salesItemId) {
        UsedStock usedStock = new UsedStock();
        usedStock.setUserId(userId);
        usedStock.setSalesItemId(salesItemId);
        return usedStock;
    }
}
