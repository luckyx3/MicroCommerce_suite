package com.hellcaster.OrderService.service;

import com.hellcaster.OrderService.model.OrderRequest;
import com.hellcaster.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
