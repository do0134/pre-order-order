package com.example.order_service.utils.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "User Not found"),
    NO_SUCH_ITEM(HttpStatus.BAD_REQUEST, "No such item"),
    NO_SUCH_ORDER(HttpStatus.BAD_REQUEST, "No such Order"),
    LOW_QUANTITY(HttpStatus.BAD_REQUEST, "Quantity is insufficient"),
    NOT_PURCHASABLE_TIME(HttpStatus.BAD_REQUEST, "Not Purchasable time"),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "Cannot access item now"),
    RANDOM_FAIL(HttpStatus.BAD_REQUEST, "랜덤 결제 실패 에러"),
    INTERNAL_ERROR(HttpStatus.BAD_REQUEST, "잘못된 접근입니다.");

    private final HttpStatus status;
    private final String message;
}
