package com.example.shoppingmall.dao;

import com.example.shoppingmall.model.*;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao extends BaseDao {

    public int add(Order order) {
        String sql = "INSERT INTO orders(orderNo, memberid, shipAddressId, shipType, payType, orderTime, amount, status) VALUES(?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, order.getOrderNo());
            pstmt.setInt(2, order.getMemberid());
            pstmt.setInt(3, order.getShipAddressId());
            pstmt.setString(4, order.getShipType());
            pstmt.setString(5, order.getPayType());
            pstmt.setTimestamp(6, new Timestamp(order.getOrderTime().getTime()));
            pstmt.setFloat(7, order.getAmount());
            pstmt.setInt(8, order.getStatus());

            int result = pstmt.executeUpdate();
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public int addOrderDetail(OrderDetail detail) {
        String sql = "INSERT INTO order_detail(goodsNo, goodsName, orderNo, price, discount, memberPrice, qty, allPrice) VALUES(?,?,?,?,?,?,?,?)";
        return executeUpdate(sql,
                detail.getGoodsNo(),
                detail.getGoodsName(),
                detail.getOrderNo(),
                detail.getPrice(),
                detail.getDiscount(),
                detail.getMemberPrice(),
                detail.getQty(),
                detail.getAllPrice());
    }

    public Order findById(Integer id) {
        String sql = "SELECT o.*, m.email, m.nick, " +
                "sa.name as shipName, sa.telno as shipTelno, sa.address as shipAddress " +
                "FROM orders o " +
                "LEFT JOIN member m ON o.memberid = m.id " +
                "LEFT JOIN ship_address sa ON o.shipAddressId = sa.id " +
                "WHERE o.id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderNo(rs.getString("orderNo"));
                order.setMemberid(rs.getInt("memberid"));
                order.setShipAddressId(rs.getInt("shipAddressId"));
                order.setShipType(rs.getString("shipType"));
                order.setPayType(rs.getString("payType"));
                order.setOrderTime(rs.getTimestamp("orderTime"));
                order.setAmount(rs.getFloat("amount"));
                order.setStatus(rs.getInt("status"));

                // 设置会员信息
                Member member = new Member();
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                order.setMember(member);

                // 设置地址信息
                ShipAddress shipAddress = new ShipAddress();
                shipAddress.setName(rs.getString("shipName"));
                shipAddress.setTelno(rs.getString("shipTelno"));
                shipAddress.setAddress(rs.getString("shipAddress"));
                order.setShipAddress(shipAddress);

                // 查询订单详情（需要修改findOrderDetails方法包含图片）
                order.setOrderDetails(findOrderDetails(order.getOrderNo()));
                return order;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public Order findByOrderNo(String orderNo) {
        String sql = "SELECT o.*, m.email, m.nick FROM orders o " +
                "LEFT JOIN member m ON o.memberid = m.id " +
                "WHERE o.orderNo=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderNo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderNo(rs.getString("orderNo"));
                order.setMemberid(rs.getInt("memberid"));
                order.setShipAddressId(rs.getInt("shipAddressId"));
                order.setShipType(rs.getString("shipType"));
                order.setPayType(rs.getString("payType"));
                order.setOrderTime(rs.getTimestamp("orderTime"));
                order.setAmount(rs.getFloat("amount"));
                order.setStatus(rs.getInt("status"));

                // 设置会员信息
                Member member = new Member();
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                order.setMember(member);

                // 查询订单详情
                order.setOrderDetails(findOrderDetails(orderNo));
                return order;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    private List<OrderDetail> findOrderDetails(String orderNo) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_detail WHERE orderNo=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderNo);
            rs = pstmt.executeQuery();

            // 初始化商品DAO，用于查询商品信息
            GoodsDao goodsDao = new GoodsDao();

            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setId(rs.getInt("id"));
                detail.setGoodsNo(rs.getString("goodsNo"));
                detail.setGoodsName(rs.getString("goodsName"));
                detail.setOrderNo(rs.getString("orderNo"));
                detail.setPrice(rs.getFloat("price"));
                detail.setDiscount(rs.getFloat("discount"));
                detail.setMemberPrice(rs.getFloat("memberPrice"));
                detail.setQty(rs.getInt("qty"));
                detail.setAllPrice(rs.getFloat("allPrice"));

                // 关键：通过goodsNo查询商品表，获取图片URL
                String goodsNo = detail.getGoodsNo();
                Goods goods = goodsDao.findByNo(goodsNo); // 假设GoodsDao有此方法
                if (goods != null) {
                    // 假设Goods实体类中有getImgUrl()方法返回图片地址
                    detail.setGoodsImg(goods.getImg());
                }

                details.add(detail);
            }
            return details;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public List<Order> findList(Integer memberId, String status, int startIndex, int size) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, m.email, m.nick FROM orders o " +
                "LEFT JOIN member m ON o.memberid = m.id WHERE o.memberid=?");

        if (status != null && !status.equals("-1")) {
            sql.append(" AND o.status=?");
        }

        sql.append(" ORDER BY o.orderTime DESC LIMIT ?,?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            pstmt.setInt(paramIndex++, memberId);

            if (status != null && !status.equals("-1")) {
                pstmt.setInt(paramIndex++, Integer.parseInt(status));
            }

            pstmt.setInt(paramIndex++, startIndex);
            pstmt.setInt(paramIndex, size);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderNo(rs.getString("orderNo"));
                order.setMemberid(rs.getInt("memberid"));
                order.setShipAddressId(rs.getInt("shipAddressId"));
                order.setShipType(rs.getString("shipType"));
                order.setPayType(rs.getString("payType"));
                order.setOrderTime(rs.getTimestamp("orderTime"));
                order.setAmount(rs.getFloat("amount"));
                order.setStatus(rs.getInt("status"));

                // 设置会员信息
                Member member = new Member();
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                order.setMember(member);

                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public int findCount(Integer memberId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE memberid=?");

        if (status != null && !status.equals("-1")) {
            sql.append(" AND status=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            pstmt.setInt(paramIndex++, memberId);

            if (status != null && !status.equals("-1")) {
                pstmt.setInt(paramIndex, Integer.parseInt(status));
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public int updateStatus(String orderNo, Integer status) {
        String sql = "UPDATE orders SET status=? WHERE orderNo=?";
        return executeUpdate(sql, status, orderNo);
    }
}