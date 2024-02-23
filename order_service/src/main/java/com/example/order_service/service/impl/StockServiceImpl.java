package com.example.order_service.service.impl;

import com.example.order_service.model.dto.Order;
import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String key = "UsedStock";
    @Override
    public void add(UsedStock usedStock) {
        redisTemplate.opsForSet().add(key, getKey(usedStock.getUserId(), usedStock.getSalesItemId()));
        log.info(String.format("User %s used stock item %s", usedStock.getUserId(), usedStock.getSalesItemId()));
    }

    @Override
    public void remove(UsedStock usedStock) {
        redisTemplate.opsForSet().remove(key, getKey(usedStock.getUserId(), usedStock.getSalesItemId()));
        log.info(String.format("User %s cancel stock item %s", usedStock.getUserId(), usedStock.getSalesItemId()));
    }

    @Override
    public Long totalUsedCount(UsedStock usedStock) {
        return redisTemplate.opsForSet().size(key);
    }

    public String getKey(Long userId, Long salesItemId) {
        return String.format("User %s used salesItem %s", userId, salesItemId);
    }
}
