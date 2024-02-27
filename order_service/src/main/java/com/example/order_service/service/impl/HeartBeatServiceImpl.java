package com.example.order_service.service.impl;

import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.service.HeartBeatService;
import com.example.order_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartBeatServiceImpl implements HeartBeatService {

    private final StockService stockService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final AtomicBoolean heartbeatAlive = new AtomicBoolean(true);

    private final TaskScheduler taskScheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final String aliveResponse = "alive";

    @Override
    public void subscribeHeartbeat(Long userId, Long salesItemId) {
        String key = getChannel(userId, salesItemId);

        redisMessageListenerContainer.addMessageListener((message, pattern) -> {
            if (aliveResponse.equals(message.toString())) {
                heartbeatAlive.set(true);
            }
        }, new ChannelTopic(getChannel(userId, salesItemId))
        );

        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> sendHeartBeat(userId, salesItemId), new CronTrigger("*/5 * * * * *"));
        scheduledTasks.put(key, scheduledTask);
    }

    private void sendHeartBeat(Long userId, Long salesItemId) {
        if (!heartbeatAlive.get()) {
            stockService.remove(UsedStock.toDto(userId, salesItemId));
            scheduledTasks.get(getChannel(userId, salesItemId)).cancel(false);
            closeHeartBeat(userId, salesItemId);
            return;
        }

        heartbeatAlive.set(false);
        redisTemplate.convertAndSend(getChannel(userId, salesItemId), getChannel(userId, salesItemId));
    }


    @Override
    public void closeHeartBeat(Long userId, Long salesItemId) {
        redisMessageListenerContainer.removeMessageListener((message, pattern) ->
                {}, new ChannelTopic(getChannel(userId, salesItemId)));
        log.info("pub/sub server closed");
    }

    private String getChannel(Long userId, Long salesItemId) {
        return String.format("User %s order Item %s", userId, salesItemId);
    }
}
