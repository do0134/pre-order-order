package com.example.order_service.model.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class OrderItem {
    private String name;
    private Long price;
    private Timestamp start_time;
    private Timestamp end_time;
}
