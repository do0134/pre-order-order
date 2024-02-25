package com.example.order_service.service;

import com.example.order_service.service.impl.HeartBeatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


@SpringBootTest
public class HeartBeatServiceTest {

    @Autowired
    private HeartBeatServiceImpl heartBeatService;

    @MockBean
    private StockService stockService;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;



}
