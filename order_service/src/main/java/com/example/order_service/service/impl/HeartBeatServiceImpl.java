package com.example.order_service.service.impl;

import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.service.HeartBeatService;
import com.example.order_service.service.StockService;
import com.example.order_service.utils.error.CustomException;
import com.example.order_service.utils.error.ErrorCode;
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

    private final String key = "heartbeat";

    @Override
    public void subscribeHeartbeat(Long userId, Long salesItemId) {
        String aliveResponse = String.format("%s is alive", getChannel(userId, salesItemId));

        redisMessageListenerContainer.addMessageListener((message, pattern) -> {
            if (aliveResponse.equals(message.toString())) {
                heartbeatAlive.set(true);
            }
        }, new ChannelTopic(key)
        );

        // 10초에 한번 실행하는 스케줄 메서드
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> sendHeartBeat(userId, salesItemId), new CronTrigger("*/10 * * * * *"));
        if (scheduledTask == null) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "스케줄 task가 비었습니다.");
        }
        // 고유 채널명을 key로 put하여 관리한다.
        scheduledTasks.put(getChannel(userId, salesItemId), scheduledTask);
    }

    private void sendHeartBeat(Long userId, Long salesItemId) {
        if (!heartbeatAlive.get()) {
            log.info(String.format("User %s cancel stock item %s", userId, salesItemId));
            scheduledTasks.get(getChannel(userId, salesItemId)).cancel(false);
            closeHeartBeat(userId, salesItemId);
            return;
        }

        stockService.add(UsedStock.toDto(userId, salesItemId));
        heartbeatAlive.set(false);
        redisTemplate.convertAndSend(key, getChannel(userId, salesItemId));
    }


    @Override
    public void closeHeartBeat(Long userId, Long salesItemId) {
        redisMessageListenerContainer.removeMessageListener((message, pattern) ->
                {}, new ChannelTopic(getChannel(userId, salesItemId)));
        scheduledTasks.get(getChannel(userId, salesItemId)).cancel(false);
        log.info("pub/sub server closed");
    }

    private String getChannel(Long userId, Long salesItemId) {
        return String.format("User %s order Item %s is alive", userId, salesItemId);
    }
}
