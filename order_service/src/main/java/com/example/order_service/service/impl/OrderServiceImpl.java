package com.example.order_service.service.impl;

import com.example.order_service.model.dto.Order;
import com.example.order_service.model.dto.OrderItem;
import com.example.order_service.model.dto.OrderUser;
import com.example.order_service.model.entity.OrderEntity;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.ItemFeignClient;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.UserFeignClient;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Order makeOrder(Long userId, Long itemId) {
        String key = getOrderRedisKey(userId, itemId);
        redisTemplate.opsForHash().putAll(key, createOrderHash(userId, itemId, 1L));
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        OrderUser orderUser = getOrderUser(userId);
        OrderItem orderItem = getOrderItem(itemId);
        checkTime(orderItem.getStart_time(), orderItem.getEnd_time());
        Long totalPrice = (long)orderItem.getPrice();
        Order order = Order.toDto(orderUser,orderItem,1L,totalPrice);
        return order;
    }

    public Order createOrder(Long userId, Long itemId) {
        String key = getOrderRedisKey(userId, itemId);
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) {
            throw new CustomException(ErrorCode.NO_SUCH_ORDER);
        }

        Long quantity = Long.valueOf((String) map.get("quantity"));
        OrderUser orderUser = getOrderUser(userId);
        OrderItem orderItem = getOrderItem(itemId);
        Long totalPrice = (long)quantity*orderItem.getPrice();
        Order order = Order.toDto(orderUser,orderItem,quantity,totalPrice);
        saveOrder(order, userId, itemId);
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
            orderList.add(Order.toDto(orderUser, orderItem, orderEntity.getQuantity(), orderEntity.getTotalPrice()));
        }

        return orderList;
    }

    @Override
    public Order getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.NO_SUCH_ORDER));
        OrderUser orderUser = getOrderUser(orderEntity.getUserId());
        OrderItem orderItem = getOrderItem(orderEntity.getItemId());
        return Order.toDto(orderUser, orderItem, orderEntity.getQuantity(), orderEntity.getTotalPrice());
    }

    @Override
//    @Transactional
    public void pay(Long userId, Long itemId) {
//            Order order = createOrder(userId, itemId);

//            String orderKey = getOrderRedisKey(userId, itemId);
//            deleteRedisKey(orderKey);

        updateStock(itemId);
    }

    private void saveOrder(Order order, Long userId, Long itemId) {
        orderRepository.save(OrderEntity.toEntity(userId, itemId, order.getTotalPrice()));
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

    private String getOrderRedisKey(Long userId, Long itemId) {
        return String.format("user%s" + "order"+"item%s",userId, itemId);
    }

    private String getItemRedisKey(Long itemId) {
        return "SalesItem:" + String.valueOf(itemId);
    }

    private void updateStock(Long itemId) {

    }

    private Map<String, Object> getRedisHash(Long itemId, Long stock) {
        String key = getItemRedisKey(itemId);
        Map<String, Object> map = new HashMap<>();
        map.put(key, String.valueOf(stock));
        return map;
    }

    private Map<String, Object> createOrderHash(Long userId, Long itemId, Long quantity) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("itemId", String.valueOf(itemId));
        map.put("quantity", String.valueOf(quantity));
        return map;
    }

    private void deleteRedisKey(String key) {
        redisTemplate.delete(key);
    }
}
