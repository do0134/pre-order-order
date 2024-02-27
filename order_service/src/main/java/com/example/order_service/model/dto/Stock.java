package com.example.order_service.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Stock {
    private Long stock;

    public Stock(Long stock) {
        this.stock = stock;
    }
}
