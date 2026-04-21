package com.example.shoppingmall.dao;


import com.example.shoppingmall.model.ShipArea;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreaDao extends BaseDao {

    public List<ShipArea> findList() {
        List<ShipArea> list = new ArrayList<>();
        String sql = "SELECT * FROM ship_area ORDER BY id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ShipArea area = new ShipArea();
                area.setId(rs.getInt("id"));
                area.setArea(rs.getString("area"));
                area.setCost(rs.getFloat("cost"));
                list.add(area);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public ShipArea findById(Integer id) {
        String sql = "SELECT * FROM ship_area WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ShipArea area = new ShipArea();
                area.setId(rs.getInt("id"));
                area.setArea(rs.getString("area"));
                area.setCost(rs.getFloat("cost"));
                return area;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }
}