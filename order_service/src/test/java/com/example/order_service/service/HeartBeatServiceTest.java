package com.example.order_service.service;

import com.example.order_service.service.impl.HeartBeatServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.mockito.Mockito.*;


@SpringBootTest
public class HeartBeatServiceTest {

    @Autowired
    private HeartBeatService heartBeatService;

    @MockBean
    private StockService stockService;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    private String successResponse = "alive";

    @Test
    void HeartBeat_Success() {
        Long userId = 1L;
        Long itemId = 1L;
        heartBeatService.subscribeHeartbeat(userId, itemId);
        verify(redisTemplate,times(1)).convertAndSend(any(), eq(successResponse));
    }
}
