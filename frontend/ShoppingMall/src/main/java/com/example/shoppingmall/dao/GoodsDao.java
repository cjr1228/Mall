package com.example.shoppingmall.dao;


import com.example.shoppingmall.model.Goods;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodsDao extends BaseDao {

    public int add(Goods goods) {
        String sql = "INSERT INTO goods(no, name, `desc`, img, price, qty, discount) VALUES(?,?,?,?,?,?,?)";
        return executeUpdate(sql,
                goods.getNo(),
                goods.getName(),
                goods.getDesc(),
                goods.getImg(),
                goods.getPrice(),
                goods.getQty(),
                goods.getDiscount());
    }

    public int update(Goods goods) {
        String sql = "UPDATE goods SET name=?, `desc`=?, img=?, price=?, qty=?, discount=? WHERE id=?";
        return executeUpdate(sql,
                goods.getName(),
                goods.getDesc(),
                goods.getImg(),
                goods.getPrice(),
                goods.getQty(),
                goods.getDiscount(),
                goods.getId());
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM goods WHERE id=?";
        return executeUpdate(sql, id);
    }

    public Goods findById(Integer id) {
        // 先检查 id 是否为 null
        if (id == null) {
            return null;
        }

        String sql = "SELECT * FROM goods WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Goods goods = new Goods();
                goods.setId(rs.getInt("id"));
                goods.setNo(rs.getString("no"));
                goods.setName(rs.getString("name"));
                goods.setDesc(rs.getString("desc"));
                goods.setImg(rs.getString("img"));
                goods.setPrice(rs.getFloat("price"));
                goods.setQty(rs.getInt("qty"));
                goods.setDiscount(rs.getFloat("discount"));
                return goods;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public Goods findByNo(String goodsNo) {
        String sql = "SELECT * FROM goods WHERE no=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, goodsNo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Goods goods = new Goods();
                goods.setId(rs.getInt("id"));
                goods.setNo(rs.getString("no"));
                goods.setName(rs.getString("name"));
                goods.setDesc(rs.getString("desc"));
                goods.setImg(rs.getString("img"));
                goods.setPrice(rs.getFloat("price"));
                goods.setQty(rs.getInt("qty"));
                goods.setDiscount(rs.getFloat("discount"));
                return goods;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public List<Goods> findList(String keyword, int startIndex, int size) {
        List<Goods> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM goods WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR `desc` LIKE ?)");
        }

        sql.append(" ORDER BY id DESC LIMIT ?,?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
                pstmt.setString(paramIndex++, "%" + keyword + "%");
            }

            pstmt.setInt(paramIndex++, startIndex);
            pstmt.setInt(paramIndex, size);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Goods goods = new Goods();
                goods.setId(rs.getInt("id"));
                goods.setNo(rs.getString("no"));
                goods.setName(rs.getString("name"));
                goods.setDesc(rs.getString("desc"));
                goods.setImg(rs.getString("img"));
                goods.setPrice(rs.getFloat("price"));
                goods.setQty(rs.getInt("qty"));
                goods.setDiscount(rs.getFloat("discount"));
                list.add(goods);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public int findCount(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM goods WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR `desc` LIKE ?)");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
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

    public int updateStock(String goodsNo, int quantity) {
        String sql = "UPDATE goods SET qty=qty-? WHERE no=? AND qty>=?";
        return executeUpdate(sql, quantity, goodsNo, quantity);
    }

    public List<Goods> findRecommendGoods(int limit) {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT * FROM goods WHERE qty > 0 ORDER BY id DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Goods goods = new Goods();
                goods.setId(rs.getInt("id"));
                goods.setNo(rs.getString("no"));
                goods.setName(rs.getString("name"));
                goods.setDesc(rs.getString("desc"));
                goods.setImg(rs.getString("img"));
                goods.setPrice(rs.getFloat("price"));
                goods.setQty(rs.getInt("qty"));
                goods.setDiscount(rs.getFloat("discount"));
                list.add(goods);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }
}