package com.example.order_service.utils.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    @Override
    public String getMessage(){
        if (message == null) {
            return errorCode.getMessage();
        }

        return String.format("%s %s", errorCode.getMessage(),message);
    }
}
