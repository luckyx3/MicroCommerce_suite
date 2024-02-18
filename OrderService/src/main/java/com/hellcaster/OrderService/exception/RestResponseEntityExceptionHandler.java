package com.hellcaster.OrderService.exception;

import com.hellcaster.OrderService.external.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//This Class will handel all the Exception to Controller that's why we annotated it with ControllerAdvice annotation
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException Exception){
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorMessage(Exception.getMessage())
                .errorCode(Exception.getErrorCode())
                .build(), HttpStatus.valueOf(Exception.getStatus()));
    }
}
