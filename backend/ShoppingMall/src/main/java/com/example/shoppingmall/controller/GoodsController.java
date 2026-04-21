package com.example.shoppingmall.controller;

import com.example.shoppingmall.service.GoodsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/goods/*")
public class GoodsController extends BaseController {
    private GoodsService goodsService = new GoodsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/detail":
                    getGoodsDetail(request, response);
                    break;
                case "/search":
                    searchGoods(request, response);
                    break;
                case "/recommend":
                    getRecommendGoods(request, response);
                    break;
                case "/checkStock":
                    checkStock(request, response);
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

    private void getGoodsDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer goodsId = getIntParameter(request, "goodsId", null);
        String goodsNo = getParameter(request, "goodsNo", "");

        // 添加详细的调试日志
        System.out.println("=== 调试信息 ===");
        System.out.println("请求URL: " + request.getRequestURL());
        System.out.println("请求参数Map: " + request.getParameterMap());
        System.out.println("获取到的 goodsId: " + goodsId);
        System.out.println("获取到的 goodsNo: " + goodsNo);

        Map<String, Object> result;
        if (goodsId != null && goodsId > 0) {
            System.out.println("使用 goodsId 路径，goodsId = " + goodsId);
            result = goodsService.getGoodsDetail(goodsId);
        } else if (!goodsNo.isEmpty()) {
            System.out.println("使用 goodsNo 路径，goodsNo = " + goodsNo);
            result = goodsService.getGoodsDetailByNo(goodsNo);
        } else {
            System.out.println("没有有效的参数");
            result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误：请提供商品ID或商品编号");
        }

        System.out.println("返回结果: " + result);
        sendJsonResponse(response, result);
    }

    private void searchGoods(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String keyword = getParameter(request, "keyword", "");
        int page = getIntParameter(request, "page", 1);
        int pageSize = getIntParameter(request, "pageSize", 12);

        Map<String, Object> result = goodsService.searchGoods(keyword, page, pageSize);
        sendJsonResponse(response, result);
    }

    private void getRecommendGoods(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int limit = getIntParameter(request, "limit", 6);

        Map<String, Object> result = goodsService.getRecommendGoods(limit);
        sendJsonResponse(response, result);
    }

    private void checkStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String goodsNo = getParameter(request, "goodsNo", "");
        int quantity = getIntParameter(request, "quantity", 1);

        Map<String, Object> result = goodsService.checkStock(goodsNo, quantity);
        sendJsonResponse(response, result);
    }
}