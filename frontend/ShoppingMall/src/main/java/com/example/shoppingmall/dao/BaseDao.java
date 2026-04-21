package com.example.shoppingmall.dao;


import com.example.shoppingmall.model.ShipAddress;
import com.example.shoppingmall.utils.DruidUtil;

import java.sql.*;

public class BaseDao {

    protected Connection getConnection() throws SQLException {
        return DruidUtil.getConnection();
    }

    protected void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    // 执行更新操作
    // 在 executeUpdate 和 queryCount 方法中，需要处理 null 参数
    protected int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) {
                    pstmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close(conn, pstmt);
        }
    }

    protected int queryCount(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) {
                    pstmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    pstmt.setObject(i + 1, params[i]);
                }
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
}