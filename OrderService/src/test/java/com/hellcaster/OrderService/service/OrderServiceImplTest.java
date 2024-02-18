package com.hellcaster.OrderService.service;

import com.hellcaster.OrderService.entity.Order;
import com.hellcaster.OrderService.exception.CustomException;
import com.hellcaster.OrderService.external.client.PaymentService;
import com.hellcaster.OrderService.external.client.ProductService;
import com.hellcaster.OrderService.external.request.PaymentRequest;
import com.hellcaster.OrderService.model.OrderRequest;
import com.hellcaster.OrderService.model.OrderResponse;
import com.hellcaster.OrderService.repository.OrderRepository;
import com.hellcaster.PaymentService.model.PaymentMode;
import com.hellcaster.PaymentService.model.PaymentResponse;
import com.hellcaster.ProductService.model.ProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();
    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){

        //Mocking
        Order order = getMockOrder();

        Mockito.when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));

        Mockito.when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+ order.getProductId(),
                        ProductResponse.class))
                .thenReturn(getMockProductResponse());

        Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+ order.getId(),
                        PaymentResponse.class))
                .thenReturn(getMockPaymentResponse());

        //Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        //Verification
        verify(orderRepository, times(1)).findById(anyLong());
        verify(restTemplate, times(1)).getForObject("http://PRODUCT-SERVICE/product/"+ order.getProductId(),
                ProductResponse.class);
        verify(restTemplate, times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/"+ order.getId(),
                PaymentResponse.class);

        //Assert
        Assertions.assertNotNull(orderResponse);
        Assertions.assertEquals(order.getId(), orderResponse.getOrderId());
    }

    @DisplayName("Get Orders - Fail Scenario")
    @Test
    void test_When_Get_Order_Not_Found_then_Not_Found(){
        //Mocking
        Mockito.when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        //Actual & Assert
        CustomException exception =
                Assertions.assertThrows(CustomException.class,
                                        () -> orderService.getOrderDetails(1));
        Assertions.assertEquals("NOT_FOUND", exception.getErrorCode());
        Assertions.assertEquals(404, exception.getStatus());

        //Verification
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        //Mocking
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        Mockito.when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Mockito.when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        Mockito.when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L,HttpStatus.OK));

        //Actual
        long orderId = orderService.placeOrder(orderRequest);

        //Verification
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        //assertion
        Assertions.assertEquals(order.getId(), orderId);
    }

    @DisplayName("Placed Order - Payment Failed Scenario")
    @Test
    void test_When_Place_Order_Payment_Failed_then_Order_Placed() {

        //Mocking
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        Mockito.when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Mockito.when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        Mockito.when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        //Actual
        long orderId = orderService.placeOrder(orderRequest);

        //Verification
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        //assertion
        Assertions.assertEquals(order.getId(), orderId);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .amount(100)
                .quantity(10)
                .paymentMode(com.hellcaster.OrderService.model.PaymentMode.CASH)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .status("ACCEPTED")
                .paymentMode(PaymentMode.CASH)
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .price(100)
                .productId(2)
                .productName("Iphone")
                .quantity(1)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .id(1)
                .OrderStatus("CREATED")
                .orderDate(Instant.now())
                .amount(100)
                .productId(2)
                .build();
    }
}