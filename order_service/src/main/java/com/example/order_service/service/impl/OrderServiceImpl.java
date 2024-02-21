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
    public Order makeOrder(Long userId, Long itemId, Long quantity) {
        String key = getOrderRedisKey(userId, itemId);
        redisTemplate.opsForHash().putAll(key, createOrderHash(userId, itemId, quantity));
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        OrderUser orderUser = getOrderUser(userId);
        OrderItem orderItem = getOrderItem(itemId);
        checkTime(orderItem.getStart_time(), orderItem.getEnd_time());
        Long totalPrice = (long)quantity*orderItem.getPrice();
        Order order = Order.toDto(orderUser,orderItem,quantity,totalPrice);
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

    public void saveOrder(Order order, Long userId, Long itemId) {
        orderRepository.save(OrderEntity.toEntity(userId, itemId, order.getQuantity(), order.getTotalPrice()));
    }

    public Boolean checkTime(Timestamp startTime, Timestamp endTime) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return now.after(startTime) && now.before(endTime);
    }

    public OrderUser getOrderUser(Long userId) {
        Response<OrderUser> orderUser = userFeignClient.getUser(userId);
        if (!orderUser.getResultCode().equals("SUCCESS")) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return orderUser.getResult();
    }

    public OrderItem getOrderItem(Long itemId) {
        Response<OrderItem> orderItem = itemFeignClient.getOrderItem(itemId);

        if (!orderItem.getResultCode().equals("SUCCESS")) {
            throw new CustomException(ErrorCode.NO_SUCH_ITEM);
        }

        return orderItem.getResult();
    }

    public String getOrderRedisKey(Long userId, Long itemId) {
        return String.format("user%s" + "order"+"item%s",userId, itemId);
    }

    public String getItemRedisKey(Long itemId) {
        return "SalesItem:" + String.valueOf(itemId);
    }

    public void updateStock(Long itemId) {

        try {
            redisTemplate.watch(getItemRedisKey(itemId));
            redisTemplate.multi();

            Object stockObject = redisTemplate.opsForHash().get("SalesItem", getItemRedisKey(itemId));
            Long stock = Long.valueOf((String) stockObject);
            if (stock <= 0L) {
                throw new CustomException(ErrorCode.NO_SUCH_ORDER);
            }

            HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
            hashOperations.putAll("SalesItem", getRedisHash(itemId, stock-1));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            redisTemplate.unwatch();
        }
    }

    public Map<String, Object> getRedisHash(Long itemId, Long stock) {
        String key = getItemRedisKey(itemId);
        Map<String, Object> map = new HashMap<>();
        map.put(key, String.valueOf(stock));
        return map;
    }

    public Map<String, Object> createOrderHash(Long userId, Long itemId, Long quantity) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("itemId", String.valueOf(itemId));
        map.put("quantity", String.valueOf(quantity));
        return map;
    }

    public void deleteRedisKey(String key) {
        redisTemplate.delete(key);
    }
}
