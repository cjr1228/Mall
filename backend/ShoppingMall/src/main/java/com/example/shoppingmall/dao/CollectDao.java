package com.example.shoppingmall.dao;


import com.example.shoppingmall.model.Collect;
import com.example.shoppingmall.model.Goods;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectDao extends BaseDao {

    public int add(Collect collect) {
        // 先检查是否已收藏
        String checkSql = "SELECT COUNT(*) FROM collect WHERE memberid=? AND goodsNo=?";
        int count = queryCount(checkSql, collect.getMemberid(), collect.getGoodsNo());

        if (count > 0) {
            return 0;
        }

        String sql = "INSERT INTO collect(memberid, goodsNo, colTime) VALUES(?,?,?)";
        return executeUpdate(sql, collect.getMemberid(), collect.getGoodsNo(), new Timestamp(System.currentTimeMillis()));
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM collect WHERE id=?";
        return executeUpdate(sql, id);
    }

    public int deleteByGoods(Integer memberId, String goodsNo) {
        String sql = "DELETE FROM collect WHERE memberid=? AND goodsNo=?";
        return executeUpdate(sql, memberId, goodsNo);
    }

    public List<Collect> findList(Integer memberId, int startIndex, int size) {
        List<Collect> list = new ArrayList<>();
        String sql = "SELECT c.*, g.name as goodsName, g.img as goodsImg, g.price, g.discount, g.no as goodsNo2 " +
                "FROM collect c " +
                "LEFT JOIN goods g ON c.goodsNo = g.no " +
                "WHERE c.memberid=? ORDER BY c.colTime DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, startIndex);
            pstmt.setInt(3, size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Collect collect = new Collect();
                collect.setId(rs.getInt("id"));
                collect.setMemberid(rs.getInt("memberid"));
                collect.setGoodsNo(rs.getString("goodsNo"));
                collect.setColTime(rs.getTimestamp("colTime"));

                // 创建并设置 Goods 对象
                Goods goods = new Goods();
                goods.setNo(rs.getString("goodsNo2"));  // 注意：这里使用 goodsNo2 别名
                goods.setName(rs.getString("goodsName"));
                goods.setImg(rs.getString("goodsImg"));
                goods.setPrice(rs.getFloat("price"));
                goods.setDiscount(rs.getFloat("discount"));
                collect.setGoods(goods);

                list.add(collect);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public int findCount(Integer memberId) {
        String sql = "SELECT COUNT(*) FROM collect WHERE memberid=?";
        return queryCount(sql, memberId);
    }

    public boolean isCollected(Integer memberId, String goodsNo) {
        String sql = "SELECT COUNT(*) FROM collect WHERE memberid=? AND goodsNo=?";
        return queryCount(sql, memberId, goodsNo) > 0;
    }
}