package com.example.shoppingmall.service;

import com.example.shoppingmall.dao.CartDao;
import com.example.shoppingmall.dao.GoodsDao;
import com.example.shoppingmall.model.Goods;
import com.example.shoppingmall.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CartService {
    private CartDao cartDao = new CartDao();
    private GoodsDao goodsDao = new GoodsDao();

    /**
     * 从Token获取用户ID
     */
    private Integer getMemberIdFromToken(HttpServletRequest request) {
        String token = JwtUtil.getTokenFromRequest(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        return JwtUtil.getMemberIdFromToken(token);
    }

    public Map<String, Object> getCartList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        List<Map<String, Object>> cartList = cartDao.findCartList(memberId);
        float totalAmount = cartDao.getCartTotalAmount(memberId);

        result.put("code", 1);
        result.put("list", cartList);
        result.put("totalAmount", totalAmount);

        return result;
    }

    public Map<String, Object> addToCart(String goodsNo, Integer quantity, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Goods goods = goodsDao.findByNo(goodsNo);
        if (goods == null) {
            result.put("code", -2);
            result.put("message", "商品不存在");
            return result;
        }

        if (goods.getQty() < quantity) {
            result.put("code", -3);
            result.put("message", "库存不足");
            return result;
        }

        int addResult = cartDao.addToCart(memberId, goodsNo, quantity);
        if (addResult > 0) {
            result.put("code", 1);
            result.put("message", "添加到购物车成功");
        } else {
            result.put("code", -4);
            result.put("message", "添加到购物车失败");
        }

        return result;
    }

    public Map<String, Object> updateCartQuantity(String goodsNo, Integer quantity, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Goods goods = goodsDao.findByNo(goodsNo);
        if (goods == null) {
            result.put("code", -2);
            result.put("message", "商品不存在");
            return result;
        }

        if (goods.getQty() < quantity) {
            result.put("code", -3);
            result.put("message", "库存不足");
            return result;
        }

        int updateResult = cartDao.updateCartQuantity(memberId, goodsNo, quantity);
        if (updateResult > 0) {
            result.put("code", 1);
            result.put("message", "更新成功");
        } else {
            result.put("code", -4);
            result.put("message", "更新失败");
        }

        return result;
    }

    public Map<String, Object> removeFromCart(String goodsNo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        int removeResult = cartDao.removeFromCart(memberId, goodsNo);

        if (removeResult > 0) {
            result.put("code", 1);
            result.put("message", "移除成功");
        } else {
            result.put("code", -2);
            result.put("message", "移除失败");
        }

        return result;
    }

    public Map<String, Object> clearCart(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        int clearResult = cartDao.clearCart(memberId);

        if (clearResult > 0) {
            result.put("code", 1);
            result.put("message", "购物车已清空");
        } else {
            result.put("code", -2);
            result.put("message", "清空失败");
        }

        return result;
    }

    public Map<String, Object> getCartCount(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            result.put("count", 0);
            return result;
        }

        int count = cartDao.getCartCount(memberId);

        result.put("code", 1);
        result.put("count", count);

        return result;
    }

    public Map<String, Object> checkCartStock(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        boolean inStock = cartDao.checkStock(memberId);

        result.put("code", 1);
        result.put("inStock", inStock);

        return result;
    }
}