package com.example.shoppingmall.controller;


import com.example.shoppingmall.service.CollectService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/collect/*")
public class CollectController extends BaseController {
    private CollectService collectService = new CollectService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/add":
                    addCollect(request, response);
                    break;
                case "/remove":
                    removeCollect(request, response);
                    break;
                default:
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("code", 404);
                    result.put("message", "接口不存在");
                    sendJsonResponse(response, result);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/list":
                    getCollectList(request, response);
                    break;
                case "/check":
                    checkCollect(request, response);
                    break;
                default:
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("code", 404);
                    result.put("message", "接口不存在");
                    sendJsonResponse(response, result);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void addCollect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");

        Map<String, Object> result = collectService.addCollect(goodsNo, request);
        sendJsonResponse(response, result);
    }

    private void removeCollect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");

        Map<String, Object> result = collectService.removeCollect(goodsNo, request);
        sendJsonResponse(response, result);
    }

    private void getCollectList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int page = getIntParameter(request, "page", 1);
        int pageSize = getIntParameter(request, "pageSize", 10);

        Map<String, Object> result = collectService.getCollectList(page, pageSize, request);
        sendJsonResponse(response, result);
    }

    private void checkCollect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");

        Map<String, Object> result = collectService.isCollected(goodsNo, request);
        sendJsonResponse(response, result);
    }
}