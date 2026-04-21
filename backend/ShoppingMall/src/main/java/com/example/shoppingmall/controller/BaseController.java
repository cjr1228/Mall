package com.example.shoppingmall.controller;

import com.example.shoppingmall.utils.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class BaseController extends HttpServlet {

    protected void sendJsonResponse(HttpServletResponse response, Map<String, Object> result)
            throws IOException {

        // 保留CORS配置，但移除Cookie相关设置
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        // 注意：不再需要 Access-Control-Allow-Credentials: true

        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(result));
    }

    protected void handleException(HttpServletResponse response, Exception e)
            throws IOException {
        e.printStackTrace();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", "系统异常: " + e.getMessage());
        sendJsonResponse(response, result);
    }

    protected String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null ? value.trim() : defaultValue;
    }

    protected Integer getIntParameter(HttpServletRequest request, String name, Integer defaultValue) {
        try {
            String value = request.getParameter(name);
            if (value == null || value.trim().isEmpty()) {
                return defaultValue; // 如果defaultValue为null，就返回null
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue; // 如果defaultValue为null，就返回null
        }
    }

    protected Float getFloatParameter(HttpServletRequest request, String name, Float defaultValue) {
        try {
            String value = request.getParameter(name);
            return value != null ? Float.parseFloat(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}