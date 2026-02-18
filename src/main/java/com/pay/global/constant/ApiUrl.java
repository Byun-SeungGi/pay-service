package com.pay.global.constant;

public final class ApiUrl {

    private ApiUrl() {}

    // v1 공통 경로
    public static final String API_V1 = "/api/v1";

    public static final String USER_URL = API_V1 + "/users";
    public static final String SIGN_UP = USER_URL + "/signUp";
    public static final String LOGIN = USER_URL + "/login";
}
