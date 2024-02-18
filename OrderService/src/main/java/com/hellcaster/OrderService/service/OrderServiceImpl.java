package com.hellcaster.OrderService.service;

import com.hellcaster.OrderService.entity.Order;
import com.hellcaster.OrderService.exception.CustomException;
import com.hellcaster.OrderService.external.client.PaymentService;
import com.hellcaster.OrderService.external.client.ProductService;
import com.hellcaster.OrderService.external.request.PaymentRequest;
import com.hellcaster.OrderService.model.OrderRequest;
import com.hellcaster.OrderService.model.OrderResponse;
import com.hellcaster.OrderService.model.PaymentMode;
import com.hellcaster.OrderService.repository.OrderRepository;
import com.hellcaster.PaymentService.model.PaymentResponse;
import com.hellcaster.ProductService.model.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with status Order Created
        //Product Service -> Block Product (Reduce the Quantity)
        //Payment Service -> Payment -> Success -> Completed , Else
        //CANCELLED
        log.info("Placeing Order Request: {}", orderRequest);

        //PRODUCT SERVICE (Using Feign Clint)
        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());
        //
        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .amount(orderRequest.getAmount())
                .quantity(orderRequest.getQuantity())
                .orderDate(Instant.now())
                .OrderStatus("CREATED")
                .build();
        order = orderRepository.save(order);

        //PAYMENT SERVICE (Using Feign Clint)
        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(order.getAmount())
                .build();

        String orderStatus = null;

        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Order Status to PLACED");
            orderStatus = "PLACED";
        }catch (Exception e){
            log.info("Error occurred in payment. Changing the Order Status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }
        //

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed successfully with Order ID: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get the Order Details for orderId: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order with given id not Found", "NOT_FOUND", 404));

        //PRODUCT SERVICE(using restTemplate)
        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());

        ProductResponse producerResponse = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/"+ order.getProductId(), ProductResponse.class);

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productName(producerResponse.getProductName())
                .productId(producerResponse.getProductId())
                .quantity(order.getQuantity())
                .price(producerResponse.getPrice())
                .build();
        //

        //PAYMENT SERVICE(using restTemplate)
        log.info("Invoking Payment service to fetch the payment for Order id: {}", orderId);

        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+ orderId, PaymentResponse.class);

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentDate(paymentResponse.getPaymentDate())
                .status(paymentResponse.getStatus())
                .paymentMode(PaymentMode.valueOf(String.valueOf(paymentResponse.getPaymentMode())))
                .build();
        //

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }
}
