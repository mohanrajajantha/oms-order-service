package com.oms.order_service.service;

import com.oms.order_service.dto.OrderRequestDto;

public interface OrderService {
    String placeOrder(OrderRequestDto orderRequest);
}