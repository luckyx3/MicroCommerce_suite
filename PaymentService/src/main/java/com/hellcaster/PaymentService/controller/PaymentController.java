package com.hellcaster.PaymentService.controller;

import com.hellcaster.PaymentService.model.PaymentRequest;
import com.hellcaster.PaymentService.model.PaymentResponse;
import com.hellcaster.PaymentService.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Log4j2
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest){
        long transactionID = paymentService.doPayment(paymentRequest);
        log.info("Payment is Completed with Transaction ID: {}", transactionID);
        return new ResponseEntity<>(transactionID, HttpStatus.OK);
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable long orderId){
        return new ResponseEntity<>(paymentService.getPaymentDetailsByOrderId(orderId), HttpStatus.OK);
    }
}
