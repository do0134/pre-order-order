package com.example.order_service.utils.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not found"),
    NO_SUCH_ITEM(HttpStatus.BAD_REQUEST, "No such item"),
    NO_SUCH_ORDER(HttpStatus.BAD_REQUEST, "No such Order"),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "Cannot access item now");

    private final HttpStatus status;
    private final String message;
}
