package com.example.order_service.service.impl;

import com.example.order_service.model.dto.*;
import com.example.order_service.model.dto.response.OrderResponse;
import com.example.order_service.model.entity.OrderEntity;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.*;
import com.example.order_service.utils.Response;
import com.example.order_service.utils.error.CustomException;
import com.example.order_service.utils.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final ItemFeignClient itemFeignClient;
    private final UserFeignClient userFeignClient;

    private final StockService stockService;
    private final HeartBeatService heartBeatService;


    @Override
    public OrderResponse createOrderCache(Long userId, Long salesItemId) {
        checkStock(userId, salesItemId);
        OrderResponse orderResponse = OrderResponse.toDto(userId, salesItemId);
        heartBeatService.subscribeHeartbeat(userId, salesItemId);
        stockService.add(UsedStock.toDto(userId, salesItemId));

        return orderResponse;
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
    @Transactional
    public OrderResponse pay(Long userId, Long itemId) {
        Random random = new Random();
        int fail = random.nextInt(100);

        if (fail <= 20) {
            throw new CustomException(ErrorCode.RANDOM_FAIL);
        }

        stockService.searchOrderCache(userId, itemId);
        OrderResponse orderResponse = OrderResponse.toDto(userId, itemId);
        itemFeignClient.updateStock(itemId);
        stockService.remove(UsedStock.toDto(userId, itemId));
        heartBeatService.closeHeartBeat(userId, itemId);
        saveOrder(userId, itemId);
        return orderResponse;
    }

    private void checkStock(Long userId, Long salesItemId) {
        Response<Stock> monoStock = itemFeignClient.getStock(salesItemId);
        Long stock = monoStock.getResult().getStock();
        if (stockService.totalUsedCount(UsedStock.toDto(userId, salesItemId)) >= stock) {
            throw new CustomException(ErrorCode.LOW_QUANTITY);
        }
    }

    private Order createOrder(Long userId, Long itemId) {
        OrderItem orderItem = getOrderItem(itemId);
        checkTime(orderItem.getStart_time(), orderItem.getEnd_time());
        OrderUser orderUser = getOrderUser(userId);

        Order order = Order.toDto(orderUser,orderItem);
        saveOrder(userId, itemId);

        return order;
    }

    private void saveOrder(Long userId, Long itemId) {
        orderRepository.save(OrderEntity.toEntity(userId, itemId));
    }

    private void checkTime(Timestamp startTime, Timestamp endTime) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if(!(now.after(startTime) && now.before(endTime))){
            throw new CustomException(ErrorCode.NOT_PURCHASABLE_TIME);
        }
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
}
