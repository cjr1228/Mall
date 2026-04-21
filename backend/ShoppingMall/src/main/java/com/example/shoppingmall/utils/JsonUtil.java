package com.example.shoppingmall.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    // 对象转JSON
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // JSON转对象
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    // JSON转列表
    public static <T> List<T> fromJsonList(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    // 创建成功响应
    public static String success(Object data) {
        return "{\"code\":200,\"message\":\"success\",\"data\":" + toJson(data) + "}";
    }

    public static String success(String message, Object data) {
        return "{\"code\":200,\"message\":\"" + message + "\",\"data\":" + toJson(data) + "}";
    }

    // 创建错误响应
    public static String error(String message) {
        return "{\"code\":500,\"message\":\"" + message + "\",\"data\":null}";
    }

    public static String error(int code, String message) {
        return "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}";
    }
}