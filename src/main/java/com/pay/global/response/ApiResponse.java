package com.pay.global.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAIL = "FAIL";

    private final String status;        // "SUCCESS" or "FAIL" or "ERROR"
    private final String message;       // 응답메시지 (ex: "회원가입 성공")
    private final T data;               // 실제데이터 (ex: DTO)

    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 성공 시 호출: 데이터가 있을 때
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(STATUS_SUCCESS, null, data);
    }

    // 성공 시 호출: 데이터 없이 메시지만 줄 때
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(STATUS_SUCCESS, message, null);
    }

    // 성공 시 호출: 데이터와 메시지 둘 다 줄 때
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(STATUS_SUCCESS, message, data);
    }

    // 실패 시 호출
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(STATUS_FAIL, message, null);
    }

}
    