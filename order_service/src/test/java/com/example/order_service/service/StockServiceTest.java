package com.example.order_service.service;

import com.example.order_service.model.dto.UsedStock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    void 재고_사용량_추가_및_제거() {
        UsedStock usedStock = UsedStock.toDto(1L, 1L);
        stockService.add(usedStock);
        assertEquals(stockService.totalUsedCount(usedStock), 1L);
        stockService.remove(usedStock);
        assertEquals(stockService.totalUsedCount(usedStock), 0L);
    }
}
