package com.example.order_service.service;

import com.example.order_service.model.dto.UsedStock;

public interface StockService {
    void add(UsedStock usedStock);
    void remove(UsedStock usedStock);
    Long totalUsedCount(UsedStock usedStock);
    void searchOrderCache(Long userId, Long salesItemId);

}
