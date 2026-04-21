package com.example.shoppingmall.dao;



import com.example.shoppingmall.model.ShipAddress;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDao extends BaseDao {

    public Integer add(ShipAddress address) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            // 添加 RETURN_GENERATED_KEYS 参数
            String sql = "INSERT INTO ship_address(memberId, name, areald, address, zipcode, telno, isDefault) VALUES(?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, address.getMemberId());
            pstmt.setString(2, address.getName());
            pstmt.setInt(3, address.getAreald());
            pstmt.setString(4, address.getAddress());
            pstmt.setString(5, address.getZipcode());
            pstmt.setString(6, address.getTelno());
            pstmt.setInt(7, address.getIsDefault());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // 获取生成的ID
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    return generatedId;
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public int update(ShipAddress address) {
        String sql = "UPDATE ship_address SET name=?, areald=?, address=?, zipcode=?, telno=?, isDefault=? WHERE id=?";
        return executeUpdate(sql,
                address.getName(),
                address.getAreald(),
                address.getAddress(),
                address.getZipcode(),
                address.getTelno(),
                address.getIsDefault(),
                address.getId());
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM ship_address WHERE id=?";
        return executeUpdate(sql, id);
    }

    public ShipAddress findById(Integer id) {
        String sql = "SELECT sa.*, a.area, a.cost FROM ship_address sa " +
                "LEFT JOIN ship_area a ON sa.areald = a.id " +
                "WHERE sa.id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ShipAddress address = new ShipAddress();
                address.setId(rs.getInt("id"));
                address.setMemberId(rs.getInt("memberId"));
                address.setName(rs.getString("name"));
                address.setAreald(rs.getInt("areald"));
                address.setAddress(rs.getString("address"));
                address.setZipcode(rs.getString("zipcode"));
                address.setTelno(rs.getString("telno"));
                address.setIsDefault(rs.getInt("isDefault"));
                return address;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public List<ShipAddress> findList(Integer memberId) {
        List<ShipAddress> list = new ArrayList<>();
        String sql = "SELECT sa.*, a.area, a.cost FROM ship_address sa " +
                "LEFT JOIN ship_area a ON sa.areald = a.id " +
                "WHERE sa.memberId=? ORDER BY sa.isDefault DESC, sa.id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ShipAddress address = new ShipAddress();
                address.setId(rs.getInt("id"));
                address.setMemberId(rs.getInt("memberId"));
                address.setName(rs.getString("name"));
                address.setAreald(rs.getInt("areald"));
                address.setAddress(rs.getString("address"));
                address.setZipcode(rs.getString("zipcode"));
                address.setTelno(rs.getString("telno"));
                address.setIsDefault(rs.getInt("isDefault"));
                list.add(address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        return list;
    }

    public int setDefaultAddress(Integer memberId, Integer addressId) {
        if (memberId == null || memberId <= 0) {
            return 0;
        }

        if (addressId == null || addressId <= 0) {
            return 0;
        }
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 清空所有默认地址
            String sql1 = "UPDATE ship_address SET isDefault=0 WHERE memberId=?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, memberId);
            pstmt1.executeUpdate();

            // 设置新的默认地址
            String sql2 = "UPDATE ship_address SET isDefault=1 WHERE id=? AND memberId=?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, addressId);
            pstmt2.setInt(2, memberId);
            int result = pstmt2.executeUpdate();

            conn.commit();
            return result;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return 0;
        }finally {
            try {
                // 先关闭Statement，再处理连接（关闭顺序：ResultSet -> Statement -> Connection）
                if (pstmt1 != null) {
                    pstmt1.close();
                }
                if (pstmt2 != null) {
                    pstmt2.close();
                }
                // 最后处理连接
                if (conn != null) {
                    conn.setAutoCommit(true); // 恢复自动提交模式
                    conn.close(); // 关闭连接
                }
            } catch (SQLException e) {
                e.printStackTrace(); // 实际项目中建议用日志框架记录
            }
        }
    }

    public ShipAddress getDefaultAddress(Integer memberId) {
        String sql = "SELECT sa.*, a.area, a.cost FROM ship_address sa " +
                "LEFT JOIN ship_area a ON sa.areald = a.id " +
                "WHERE sa.memberId=? AND sa.isDefault=1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ShipAddress address = new ShipAddress();
                address.setId(rs.getInt("id"));
                address.setMemberId(rs.getInt("memberId"));
                address.setName(rs.getString("name"));
                address.setAreald(rs.getInt("areald"));
                address.setAddress(rs.getString("address"));
                address.setZipcode(rs.getString("zipcode"));
                address.setTelno(rs.getString("telno"));
                address.setIsDefault(rs.getInt("isDefault"));
                return address;
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