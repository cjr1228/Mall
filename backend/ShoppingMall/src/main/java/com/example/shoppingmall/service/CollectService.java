package com.example.shoppingmall.service;

import com.example.shoppingmall.dao.CollectDao;
import com.example.shoppingmall.dao.GoodsDao;
import com.example.shoppingmall.model.Collect;
import com.example.shoppingmall.model.Goods;
import com.example.shoppingmall.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CollectService {
    private CollectDao collectDao = new CollectDao();
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

    public Map<String, Object> addCollect(String goodsNo, HttpServletRequest request) {
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

        if (collectDao.isCollected(memberId, goodsNo)) {
            result.put("code", -3);
            result.put("message", "已收藏");
            return result;
        }

        Collect collect = new Collect(memberId, goodsNo);
        int addResult = collectDao.add(collect);

        if (addResult > 0) {
            result.put("code", 1);
            result.put("message", "收藏成功");
        } else {
            result.put("code", -4);
            result.put("message", "收藏失败");
        }

        return result;
    }

    public Map<String, Object> removeCollect(String goodsNo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        int removeResult = collectDao.deleteByGoods(memberId, goodsNo);

        if (removeResult > 0) {
            result.put("code", 1);
            result.put("message", "取消收藏成功");
        } else {
            result.put("code", -2);
            result.put("message", "取消收藏失败");
        }

        return result;
    }

    public Map<String, Object> getCollectList(int page, int pageSize, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        if (page < 1) page = 1;
        int startIndex = (page - 1) * pageSize;

        List<Collect> collects = collectDao.findList(memberId, startIndex, pageSize);
        int totalCount = collectDao.findCount(memberId);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        result.put("code", 1);
        result.put("list", collects);
        result.put("total", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPage", totalPage);

        return result;
    }

    public Map<String, Object> isCollected(String goodsNo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            result.put("isCollected", false);
            return result;
        }

        boolean isCollected = collectDao.isCollected(memberId, goodsNo);

        result.put("code", 1);
        result.put("isCollected", isCollected);

        return result;
    }
}