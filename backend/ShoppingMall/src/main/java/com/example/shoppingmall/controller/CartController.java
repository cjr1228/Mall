package com.example.shoppingmall.controller;


import com.example.shoppingmall.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/cart/*")
public class CartController extends BaseController {
    private CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/add":
                    addToCart(request, response);
                    break;
                case "/update":
                    updateCart(request, response);
                    break;
                case "/remove":
                    removeFromCart(request, response);
                    break;
                case "/clear":
                    clearCart(request, response);
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
                    getCartList(request, response);
                    break;
                case "/count":
                    getCartCount(request, response);
                    break;
                case "/checkStock":
                    checkCartStock(request, response);
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

    private void addToCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");
        int quantity = getIntParameter(request, "quantity", 1);

        Map<String, Object> result = cartService.addToCart(goodsNo, quantity, request);
        sendJsonResponse(response, result);
    }

    private void getCartList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = cartService.getCartList(request);
        sendJsonResponse(response, result);
    }

    private void updateCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");
        int quantity = getIntParameter(request, "quantity", 1);

        Map<String, Object> result = cartService.updateCartQuantity(goodsNo, quantity, request);
        sendJsonResponse(response, result);
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");

        Map<String, Object> result = cartService.removeFromCart(goodsNo, request);
        sendJsonResponse(response, result);
    }

    private void clearCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = cartService.clearCart(request);
        sendJsonResponse(response, result);
    }

    private void getCartCount(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = cartService.getCartCount(request);
        sendJsonResponse(response, result);
    }

    private void checkCartStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = cartService.checkCartStock(request);
        sendJsonResponse(response, result);
    }
}