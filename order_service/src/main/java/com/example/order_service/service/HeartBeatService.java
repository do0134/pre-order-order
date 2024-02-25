package com.example.order_service.service;

public interface HeartBeatService {
    void subscribeHeartbeat(Long userId, Long salesItemId);

    void closeHeartBeat(Long userId, Long salesItemId);
}
