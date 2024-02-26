package com.example.order_service.service;

import com.example.order_service.model.dto.Order;

import com.example.order_service.utils.error.CustomException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
public class OrderServiceTest {


//    @MockBean
//    private RedisConfig redisConfig;
//
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private OrderService orderService;
//
//    private static String key = "UsedStock";

    @Test
    void 주문_성공() {

        // Set
        Long userId = 1L;
        Long itemId = 1L;


        // Redis에 order가 있다고 하고(Mock하고)
        Mockito.when(redisTemplate.opsForSet().isMember("UsedStock" + itemId, getRedisKey(userId, itemId))).thenReturn(true);
        // StockService의 remove는 했다 치고
//        Mockito.doNothing().when(stockService).remove(UsedStock.toDto(userId, itemId));

        Order order = orderService.createOrder(userId, itemId);

        System.out.println(order);

        assertNotNull(order);
    }

    @Test
    void 주문_실패() {
        Long userId = 1L;
        Long itemId = 1L;

        Map<Object, Boolean> resultMap = new HashMap<>();
        resultMap.put("key" + itemId, false);

        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        
        Mockito.when(redisTemplate.opsForSet().isMember(any(), any())).thenReturn(resultMap);
        
        assertThrows(CustomException.class, () -> orderService.createOrder(userId, itemId));
    }

    private String getRedisKey(Long userId, Long salesItemId) {
        return String.format("User %s used salesItem %s", userId, salesItemId);
    }

}
