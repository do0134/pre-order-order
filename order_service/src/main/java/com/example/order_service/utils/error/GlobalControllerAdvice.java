package com.example.order_service.utils.error;

import com.example.order_service.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(CustomException.class)
    public Response<?> errorHandler(CustomException e) {
        log.error("Error occured {}", e.toString());
        return Response.error(e.getErrorCode().toString(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Response<?> applicationHandler(RuntimeException e) {
        log.error("Error occured {}", e.toString());
        return Response.error(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
    }
}
