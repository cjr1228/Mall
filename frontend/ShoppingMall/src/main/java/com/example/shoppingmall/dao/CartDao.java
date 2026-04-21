package com.example.shoppingmall.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartDao extends BaseDao {

    public int addToCart(Integer memberId, String goodsNo, Integer quantity) {
        // 先检查是否已存在
        String checkSql = "SELECT COUNT(*) FROM cart WHERE memberid=? AND goodsNo=?";
        int count = queryCount(checkSql, memberId, goodsNo);

        if (count > 0) {
            // 更新数量
            String updateSql = "UPDATE cart SET quantity=quantity+? WHERE memberid=? AND goodsNo=?";
            return executeUpdate(updateSql, quantity, memberId, goodsNo);
        } else {
            // 新增
            String insertSql = "INSERT INTO cart(memberid, goodsNo, quantity, addTime) VALUES(?,?,?,?)";
            return executeUpdate(insertSql, memberId, goodsNo, quantity, new Timestamp(System.currentTimeMillis()));
        }
    }

    public int updateCartQuantity(Integer memberId, String goodsNo, Integer quantity) {
        String sql = "UPDATE cart SET quantity=? WHERE memberid=? AND goodsNo=?";
        return executeUpdate(sql, quantity, memberId, goodsNo);
    }

    public int removeFromCart(Integer memberId, String goodsNo) {
        String sql = "DELETE FROM cart WHERE memberid=? AND goodsNo=?";
        return executeUpdate(sql, memberId, goodsNo);
    }

    public int clearCart(Integer memberId) {
        String sql = "DELETE FROM cart WHERE memberid=?";
        return executeUpdate(sql, memberId);
    }

    public List<Map<String, Object>> findCartList(Integer memberId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.*, g.name, g.img, g.price, g.discount, g.qty as stock " +
                "FROM cart c " +
                "LEFT JOIN goods g ON c.goodsNo = g.no " +
                "WHERE c.memberid=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("memberid", rs.getInt("memberid"));
                item.put("goodsNo", rs.getString("goodsNo"));
                item.put("goodsName", rs.getString("name"));
                item.put("goodsImg", rs.getString("img"));
                item.put("price", rs.getFloat("price"));
                item.put("discount", rs.getFloat("discount"));
                item.put("memberPrice", rs.getFloat("price") * rs.getFloat("discount"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("stock", rs.getInt("stock"));
                item.put("addTime", rs.getTimestamp("addTime"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public int getCartCount(Integer memberId) {
        String sql = "SELECT COUNT(*) FROM cart WHERE memberid=?";
        return queryCount(sql, memberId);
    }

    public float getCartTotalAmount(Integer memberId) {
        String sql = "SELECT SUM(g.price * g.discount * c.quantity) as total " +
                "FROM cart c " +
                "LEFT JOIN goods g ON c.goodsNo = g.no " +
                "WHERE c.memberid=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getFloat("total");
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public boolean checkStock(Integer memberId) {
        String sql = "SELECT COUNT(*) as outOfStock " +
                "FROM cart c " +
                "LEFT JOIN goods g ON c.goodsNo = g.no " +
                "WHERE c.memberid=? AND c.quantity > g.qty";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("outOfStock") == 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(conn, pstmt, rs);
        }
    }
}