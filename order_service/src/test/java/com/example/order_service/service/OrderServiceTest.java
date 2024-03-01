package com.example.order_service.service;

import com.example.order_service.model.dto.OrderItem;
import com.example.order_service.model.dto.OrderUser;
import com.example.order_service.model.dto.response.OrderResponse;
import com.example.order_service.utils.Response;
import com.example.order_service.utils.error.CustomException;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
public class OrderServiceTest {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OrderService orderService;

    @MockBean
    private ItemFeignClient itemFeignClient;

    @MockBean
    private UserFeignClient userFeignClient;


    @Test
    void 주문_성공() {

        // Set
        Long userId = 1L;
        Long itemId = 1L;

        OrderUser orderUser = new OrderUser();
        OrderItem orderItem = new OrderItem();

        redisTemplate.opsForSet().add("UsedStock" + itemId, getRedisKey(userId, itemId));
        Mockito.when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(orderUser));
        Mockito.when(itemFeignClient.getOrderItem(anyLong())).thenReturn(Response.success(orderItem));

        // then
        OrderResponse orderResponse = orderService.pay(userId, itemId);

        assertNotNull(orderResponse);

        redisTemplate.opsForSet().remove("UsedStock" + itemId, getRedisKey(userId, itemId));
    }

    @Test
    void 주문_실패() {
        Long userId = 1L;
        Long itemId = 1L;

        assertThrows(CustomException.class, () -> orderService.pay(userId, itemId));
    }

    private String getRedisKey(Long userId, Long salesItemId) {
        return String.format("User %s used salesItem %s", userId, salesItemId);
    }

}
