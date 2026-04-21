package com.example.shoppingmall.service;


import com.example.shoppingmall.dao.GoodsDao;
import com.example.shoppingmall.model.Goods;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GoodsService {
    private GoodsDao goodsDao = new GoodsDao();

    public Map<String, Object> getGoodsDetail(Integer goodsId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证 goodsId 是否为 null
            if (goodsId == null) {
                result.put("code", -1);
                result.put("message", "商品ID不能为空");
                return result;
            }

            // 验证 goodsId 是否有效
            if (goodsId <= 0) {
                result.put("code", -1);
                result.put("message", "商品ID无效");
                return result;
            }

            Goods goods = goodsDao.findById(goodsId);

            if (goods != null) {
                result.put("code", 1);
                result.put("message", "获取成功");
                result.put("goods", goods);
            } else {
                result.put("code", -1);
                result.put("message", "商品不存在");
            }

        } catch (Exception e) {
            // 捕获所有异常，包括 SQL 异常
            System.err.println("获取商品详情异常: " + e.getMessage());
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "系统异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getGoodsDetailByNo(String goodsNo) {
        Map<String, Object> result = new HashMap<>();
        Goods goods = goodsDao.findByNo(goodsNo);

        if (goods != null) {
            result.put("code", 1);
            result.put("message", "获取成功");
            result.put("goods", goods);
        } else {
            result.put("code", -1);
            result.put("message", "商品不存在");
        }

        return result;
    }

    public Map<String, Object> searchGoods(String keyword, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        if (page < 1) page = 1;
        int startIndex = (page - 1) * pageSize;

        List<Goods> goodsList = goodsDao.findList(keyword, startIndex, pageSize);
        int totalCount = goodsDao.findCount(keyword);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        result.put("code", 1);
        result.put("list", goodsList);
        result.put("total", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPage", totalPage);

        return result;
    }

    public Map<String, Object> getRecommendGoods(int limit) {
        Map<String, Object> result = new HashMap<>();
        List<Goods> goodsList = goodsDao.findRecommendGoods(limit);

        result.put("code", 1);
        result.put("list", goodsList);

        return result;
    }

    public Map<String, Object> checkStock(String goodsNo, int quantity) {
        Map<String, Object> result = new HashMap<>();
        Goods goods = goodsDao.findByNo(goodsNo);

        if (goods == null) {
            result.put("code", -1);
            result.put("message", "商品不存在");
            result.put("inStock", false);
        } else if (goods.getQty() < quantity) {
            result.put("code", 0);
            result.put("message", "库存不足");
            result.put("inStock", false);
            result.put("availableQty", goods.getQty());
        } else {
            result.put("code", 1);
            result.put("message", "库存充足");
            result.put("inStock", true);
        }

        return result;
    }
}