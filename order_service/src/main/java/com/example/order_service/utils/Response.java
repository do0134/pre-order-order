package com.example.order_service.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response <T>{
    private String resultCode;
    private T result;

    public static <T> Response<T> success() {
        return new Response<>("SUCCESS", null);
    }

    public static <T> Response<T> success(T result) {
        return new Response<>("SUCCESS", result);
    }

    public static Response<String> error(String resultCode, String result) {
        return new Response<>(resultCode, result);
    }

    public String toStream() {
        if (result == null) {
            return "{" + "\"resultCode\":" + "\""+ resultCode + "\"" +
                    "\"result\":" + null + "}";
        }

        return "{" + "\"resultCode\":" + "\""+ resultCode + "\"" +
                "\"result\":" + result + "}";
    }
}

