package com.hellcaster.OrderService.exception;

import lombok.Data;

@Data
public class CustomException extends RuntimeException{
    private String errorCode;
    private int status;

    public CustomException (String errorMessage, String errorCode, int status){
        super(errorMessage);
        this.status = status;
        this.errorCode = errorCode;
    }
}
