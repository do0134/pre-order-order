package com.example.order_service.service.impl;

import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.service.StockService;
import com.example.order_service.utils.error.CustomException;
import com.example.order_service.utils.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String key = "UsedStock";
    @Override
    public void add(UsedStock usedStock) {
        redisTemplate.opsForSet().add(key+usedStock.getSalesItemId(), getKey(usedStock.getUserId(), usedStock.getSalesItemId()));
        redisTemplate.expire(key+usedStock.getSalesItemId(), 120, TimeUnit.SECONDS);
        log.info(String.format("User %s used stock item %s", usedStock.getUserId(), usedStock.getSalesItemId()));
    }

    @Override
    public void remove(UsedStock usedStock) {
        redisTemplate.opsForSet().remove(key+usedStock.getSalesItemId(), getKey(usedStock.getUserId(), usedStock.getSalesItemId()));

    }

    @Override
    public Long totalUsedCount(UsedStock usedStock) {
        return redisTemplate.opsForSet().size(key+usedStock.getSalesItemId());
    }

    @Override
    public void searchOrderCache(Long userId, Long salesItemId) {
        if (Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(key + salesItemId, getKey(userId, salesItemId)))) {
            throw new CustomException(ErrorCode.NO_SUCH_ORDER);
        }
    }

    public String getKey(Long userId, Long salesItemId) {
        return String.format("User %s used salesItem %s", userId, salesItemId);
    }
}
