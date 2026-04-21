package com.example.shoppingmall.controller;

import com.example.shoppingmall.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/order/*")
public class OrderController extends BaseController {
    private OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/create":
                    createOrder(request, response);
                    break;
                case "/createNow":
                    createOrderNow(request, response);
                    break;
                case "/pay":
                    payOrder(request, response);
                    break;
                case "/cancel":
                    cancelOrder(request, response);
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
                    getOrderList(request, response);
                    break;
                case "/detail":
                    getOrderDetail(request, response);
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

    private void createOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer addressId = getIntParameter(request, "addressId", null);
        String shipType = getParameter(request, "shipType", "1");
        String payType = getParameter(request, "payType", "1");

        Map<String, Object> result = orderService.createOrder(addressId, shipType, payType, request);
        sendJsonResponse(response, result);
    }

    private void createOrderNow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");
        Integer quantity = getIntParameter(request, "quantity", 1);
        Integer addressId = getIntParameter(request, "addressId", null);
        String shipType = getParameter(request, "shipType", "1");
        String payType = getParameter(request, "payType", "1");

        Map<String, Object> result = orderService.createOrderNow(goodsNo, quantity, addressId, shipType, payType, request);
        sendJsonResponse(response, result);
    }

    private void getOrderList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String status = getParameter(request, "status", "-1");
        int page = getIntParameter(request, "page", 1);
        int pageSize = getIntParameter(request, "pageSize", 10);

        Map<String, Object> result = orderService.getOrderList(status, page, pageSize, request);
        sendJsonResponse(response, result);
    }

    private void getOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer orderId = getIntParameter(request, "orderId", null);
        String orderNo = getParameter(request, "orderNo", "");

        Map<String, Object> result;
        if (orderId != null) {
            result = orderService.getOrderDetail(orderId, request);
        } else if (!orderNo.isEmpty()) {
            result = orderService.getOrderDetailByNo(orderNo, request);
        } else {
            result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误");
        }

        sendJsonResponse(response, result);
    }

    private void payOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String orderNo = getParameter(request, "orderNo", "");

        Map<String, Object> result = orderService.payOrder(orderNo, request);
        sendJsonResponse(response, result);
    }

    private void cancelOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer orderId = getIntParameter(request, "orderId", null);

        Map<String, Object> result = orderService.cancelOrder(orderId, request);
        sendJsonResponse(response, result);
    }


}