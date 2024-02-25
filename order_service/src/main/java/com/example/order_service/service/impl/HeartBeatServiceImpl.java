package com.example.order_service.service.impl;

import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.service.HeartBeatService;
import com.example.order_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartBeatServiceImpl implements HeartBeatService {

    private final StockService stockService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final AtomicBoolean heartbeatAlive = new AtomicBoolean(true);

    private final String aliveResponse = "alive";

    @Override
    public void subscribeHeartbeat(Long userId, Long salesItemId) {
        redisMessageListenerContainer.addMessageListener((message, pattern) -> {
            if (aliveResponse.equals(message.toString())) {
                heartbeatAlive.set(true);
            }

        }, new ChannelTopic(getChannel(userId, salesItemId))
        );

        sendHeartBeat(userId, salesItemId);
    }

    @Scheduled(fixedRate = 5000)
    private void sendHeartBeat(Long userId, Long salesItemId) {
        if (!heartbeatAlive.get()) {
            stockService.remove(UsedStock.toDto(userId, salesItemId));
            closeHeartBeat(userId, salesItemId);
            return;
        }
        heartbeatAlive.set(false);
        redisTemplate.convertAndSend(getChannel(userId, salesItemId), "alive");
    }


    @Override
    public void closeHeartBeat(Long userId, Long salesItemId) {
        redisMessageListenerContainer.removeMessageListener((message, pattern) ->
                {}, new ChannelTopic(getChannel(userId, salesItemId)));
    }

    private String getChannel(Long userId, Long salesItemId) {
        return String.format("User %s order Item %s", userId, salesItemId);
    }
}
