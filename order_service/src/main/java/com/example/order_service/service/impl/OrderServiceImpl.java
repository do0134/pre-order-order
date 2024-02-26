package com.example.order_service.service.impl;

import com.example.order_service.model.dto.Order;
import com.example.order_service.model.dto.OrderItem;
import com.example.order_service.model.dto.OrderUser;
import com.example.order_service.model.dto.UsedStock;
import com.example.order_service.model.entity.OrderEntity;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.*;
import com.example.order_service.utils.Response;
import com.example.order_service.utils.error.CustomException;
import com.example.order_service.utils.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final ItemFeignClient itemFeignClient;
    private final UserFeignClient userFeignClient;

    private final StockService stockService;
    private final HeartBeatService heartBeatService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final static String key = "UsedStock";

    @Override
    @Transactional
    public Order createOrder(Long userId, Long itemId) {
        Boolean isOrderExist = redisTemplate.opsForSet().isMember(key + itemId, getRedisKey(userId,itemId));

        if (Boolean.FALSE.equals(isOrderExist)) {
            throw new CustomException(ErrorCode.NO_SUCH_ORDER);
        }

        OrderUser orderUser = getOrderUser(userId);
        OrderItem orderItem = getOrderItem(itemId);

        Order order = Order.toDto(orderUser,orderItem);
        saveOrder(userId, itemId);

        stockService.remove(UsedStock.toDto(userId, itemId));
        return order;
    }

    @Override
    public List<Order> getUserOrder(Long userId) {
        Optional<List<OrderEntity>> orderEntityList = orderRepository.findAllByUserId(userId);
        List<Order> orderList = new ArrayList<>();

        if (orderEntityList.isEmpty()) {
            return orderList;
        }

        for (OrderEntity orderEntity:orderEntityList.get()) {
            OrderUser orderUser = getOrderUser(orderEntity.getUserId());
            OrderItem orderItem = getOrderItem(orderEntity.getItemId());
            orderList.add(Order.toDto(orderUser, orderItem));
        }

        return orderList;
    }

    @Override
    public Order getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.NO_SUCH_ORDER));
        OrderUser orderUser = getOrderUser(orderEntity.getUserId());
        OrderItem orderItem = getOrderItem(orderEntity.getItemId());
        return Order.toDto(orderUser, orderItem);
    }

    @Override
//    @Transactional
    public void pay(Long userId, Long itemId) {
//            Order order = createOrder(userId, itemId);

//            String orderKey = getOrderRedisKey(userId, itemId);
//            deleteRedisKey(orderKey);

    }

    private void saveOrder(Long userId, Long itemId) {
        orderRepository.save(OrderEntity.toEntity(userId, itemId));
    }

    private Boolean checkTime(Timestamp startTime, Timestamp endTime) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return now.after(startTime) && now.before(endTime);
    }

    private OrderUser getOrderUser(Long userId) {
        Response<OrderUser> orderUser = userFeignClient.getUser(userId);
        if (!orderUser.getResultCode().equals("SUCCESS")) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return orderUser.getResult();
    }

    private OrderItem getOrderItem(Long itemId) {
        Response<OrderItem> orderItem = itemFeignClient.getOrderItem(itemId);

        if (!orderItem.getResultCode().equals("SUCCESS")) {
            throw new CustomException(ErrorCode.NO_SUCH_ITEM);
        }

        return orderItem.getResult();
    }

    private String getRedisKey(Long userId, Long salesItemId) {
        return String.format("User %s used salesItem %s", userId, salesItemId);
    }

}
