package com.example.order_service.service;

public interface HeartBeatService {
    void sendHeartBeat();
    void checkHeartBeat();
    void closeHeartBeat();
}
