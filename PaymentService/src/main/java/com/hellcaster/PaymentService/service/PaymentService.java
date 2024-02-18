package com.hellcaster.PaymentService.service;

import com.hellcaster.PaymentService.model.PaymentRequest;
import com.hellcaster.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);

}
