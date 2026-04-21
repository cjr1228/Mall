package com.example.shoppingmall.service;

import com.example.shoppingmall.dao.AddressDao;
import com.example.shoppingmall.dao.CartDao;
import com.example.shoppingmall.dao.GoodsDao;
import com.example.shoppingmall.dao.OrderDao;
import com.example.shoppingmall.model.Order;
import com.example.shoppingmall.model.Member;
import com.example.shoppingmall.model.OrderDetail;
import com.example.shoppingmall.model.ShipAddress;
import com.example.shoppingmall.model.Goods;
import com.example.shoppingmall.utils.CommonUtil;
import com.example.shoppingmall.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class OrderService {
    private OrderDao orderDao = new OrderDao();
    private GoodsDao goodsDao = new GoodsDao();
    private CartDao cartDao = new CartDao();
    private AddressDao addressDao = new AddressDao();

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

    /**
     * 立即购买创建订单
     */
    public Map<String, Object> createOrderNow(String goodsNo, Integer quantity, Integer addressId,
                                              String shipType, String payType, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        // 验证地址
        ShipAddress address = addressDao.findById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "收货地址无效");
            return result;
        }

        // 检查商品库存
        Goods goods = goodsDao.findByNo(goodsNo);
        if (goods == null) {
            result.put("code", -3);
            result.put("message", "商品不存在");
            return result;
        }

        if (goods.getQty() < quantity) {
            result.put("code", -4);
            result.put("message", "库存不足");
            result.put("availableQty", goods.getQty());
            return result;
        }

        // 计算订单金额
        float totalAmount = goods.getMemberPrice() * quantity;
        float shippingCost = 5.0f; // 运费
        totalAmount += shippingCost;

        // 创建订单
        Order order = new Order();
        order.setOrderNo(CommonUtil.generateOrderNo());
        order.setMemberid(memberId);
        order.setShipAddressId(addressId);
        order.setShipType(shipType);
        order.setPayType(payType);
        order.setOrderTime(new Date());
        order.setAmount(totalAmount);
        order.setStatus(0);

        try {
            int orderId = orderDao.add(order);
            if (orderId <= 0) {
                result.put("code", -5);
                result.put("message", "订单创建失败");
                return result;
            }

            // 创建订单详情
            OrderDetail detail = new OrderDetail();
            detail.setGoodsNo(goodsNo);
            detail.setGoodsName(goods.getName());
            detail.setOrderNo(order.getOrderNo());
            detail.setPrice(goods.getPrice());
            detail.setDiscount(goods.getDiscount());
            detail.setMemberPrice(goods.getMemberPrice());
            detail.setQty(quantity);
            detail.setAllPrice(goods.getMemberPrice() * quantity);

            orderDao.addOrderDetail(detail);

            // 更新库存
            goodsDao.updateStock(goodsNo, quantity);

            result.put("code", 1);
            result.put("message", "订单创建成功");
            result.put("orderNo", order.getOrderNo());
            result.put("orderId", orderId);
            result.put("amount", totalAmount);
            result.put("shippingCost", shippingCost);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", -6);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 从购物车创建订单
     */
    public Map<String, Object> createOrder(Integer addressId, String shipType, String payType, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress address = addressDao.findById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "收货地址无效");
            return result;
        }

        List<Map<String, Object>> cartItems = cartDao.findCartList(memberId);
        if (cartItems == null || cartItems.isEmpty()) {
            result.put("code", -3);
            result.put("message", "购物车为空");
            return result;
        }

        if (!cartDao.checkStock(memberId)) {
            result.put("code", -4);
            result.put("message", "部分商品库存不足");
            return result;
        }

        float totalAmount = cartDao.getCartTotalAmount(memberId);
        if (totalAmount <= 0) {
            result.put("code", -5);
            result.put("message", "金额计算错误");
            return result;
        }

        // 添加运费
        float shippingCost = 5.0f;
        totalAmount += shippingCost;

        Order order = new Order();
        order.setOrderNo(CommonUtil.generateOrderNo());
        order.setMemberid(memberId);
        order.setShipAddressId(addressId);
        order.setShipType(shipType);
        order.setPayType(payType);
        order.setOrderTime(new Date());
        order.setAmount(totalAmount);
        order.setStatus(0);

        try {
            int orderId = orderDao.add(order);
            if (orderId <= 0) {
                result.put("code", -6);
                result.put("message", "订单创建失败");
                return result;
            }

            List<OrderDetail> orderDetails = new ArrayList<>();
            for (Map<String, Object> item : cartItems) {
                String goodsNo = (String) item.get("goodsNo");
                int quantity = (int) item.get("quantity");
                float price = (float) item.get("price");
                float discount = (float) item.get("discount");
                float memberPrice = price * discount;

                OrderDetail detail = new OrderDetail();
                detail.setGoodsNo(goodsNo);
                detail.setGoodsName((String) item.get("goodsName"));
                detail.setOrderNo(order.getOrderNo());
                detail.setPrice(price);
                detail.setDiscount(discount);
                detail.setMemberPrice(memberPrice);
                detail.setQty(quantity);
                detail.setAllPrice(memberPrice * quantity);

                orderDao.addOrderDetail(detail);
                orderDetails.add(detail);

                goodsDao.updateStock(goodsNo, quantity);
            }

            order.setOrderDetails(orderDetails);
            cartDao.clearCart(memberId);

            result.put("code", 1);
            result.put("message", "订单创建成功");
            result.put("orderNo", order.getOrderNo());
            result.put("orderId", orderId);
            result.put("amount", totalAmount);
            result.put("shippingCost", shippingCost);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", -7);
            result.put("message", "系统错误");
        }

        return result;
    }

    public Map<String, Object> getOrderList(String status, int page, int pageSize, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        if (page < 1) page = 1;
        int startIndex = (page - 1) * pageSize;

        List<Order> orders = orderDao.findList(memberId, status, startIndex, pageSize);
        int totalCount = orderDao.findCount(memberId, status);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        result.put("code", 1);
        result.put("list", orders);
        result.put("total", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPage", totalPage);

        return result;
    }

    public Map<String, Object> getOrderDetail(Integer orderId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Order order = orderDao.findById(orderId);

        if (order != null && order.getMemberid().equals(memberId)) {
            result.put("code", 1);
            result.put("order", order);
        } else {
            result.put("code", -2);
            result.put("message", "订单不存在或无权查看");
        }

        return result;
    }

    public Map<String, Object> getOrderDetailByNo(String orderNo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Order order = orderDao.findByOrderNo(orderNo);

        if (order != null && order.getMemberid().equals(memberId)) {
            result.put("code", 1);
            result.put("order", order);
        } else {
            result.put("code", -2);
            result.put("message", "订单不存在或无权查看");
        }

        return result;
    }

    public Map<String, Object> payOrder(String orderNo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Order order = orderDao.findByOrderNo(orderNo);

        if (order == null || !order.getMemberid().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "订单不存在");
            return result;
        }

        if (order.getStatus() != 0) {
            result.put("code", -3);
            result.put("message", "订单状态错误");
            return result;
        }

        int payResult = orderDao.updateStatus(orderNo, 1);
        if (payResult > 0) {
            result.put("code", 1);
            result.put("message", "支付成功");
        } else {
            result.put("code", -4);
            result.put("message", "支付失败");
        }

        return result;
    }

    public Map<String, Object> cancelOrder(Integer orderId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Order order = orderDao.findById(orderId);

        if (order == null || !order.getMemberid().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "订单不存在");
            return result;
        }

        if (order.getStatus() != 0) {
            result.put("code", -3);
            result.put("message", "只能取消未支付的订单");
            return result;
        }

        try {
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    // 取消订单，恢复库存
                    goodsDao.updateStock(detail.getGoodsNo(), -detail.getQty());
                }
            }

            // 修正：取消订单状态应该为4，而不是2
            int cancelResult = orderDao.updateStatus(order.getOrderNo(), 4); // 改为4
            if (cancelResult > 0) {
                result.put("code", 1);
                result.put("message", "订单取消成功");
            } else {
                result.put("code", -4);
                result.put("message", "订单取消失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", -5);
            result.put("message", "系统错误");
        }

        return result;
    }


}