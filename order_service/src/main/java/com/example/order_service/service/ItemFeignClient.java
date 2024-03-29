package com.example.order_service.service;

import com.example.order_service.model.dto.OrderItem;
import com.example.order_service.model.dto.Stock;
import com.example.order_service.utils.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

@FeignClient(name = "ItemToOrderFeignClient", url = "http://localhost:8084/api/v1/internal/item")
public interface ItemFeignClient {
    @RequestMapping(method = RequestMethod.GET,value = "/order/{itemId}")
    Response<OrderItem> getOrderItem(@PathVariable("itemId") Long itemId);

    @RequestMapping(method =  RequestMethod.GET, value = "/stock/{itemId}")
    Response<Stock> getStock(@PathVariable("itemId") Long itemId);

    @RequestMapping(method = RequestMethod.PUT, value = "/order/{itemId}")
    Response<Void> updateStock(@PathVariable("itemId") Long itemId);
}
