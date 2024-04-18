package com.gdxydgyhlw.heyuan.http;

public enum Service {
    BASE_URL,LOGIN;
    public static String getBaseUrl() {
        return "https://itao-tech.com/api/";
    }
    public static String getLoginUrl() {
        return "login";
    }
    public static final String COOKIE = "cookie";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String LOGIN_NAME = "login_name";
}
