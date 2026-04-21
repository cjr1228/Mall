package com.example.shoppingmall.dao;



import com.example.shoppingmall.model.Member;
import com.example.shoppingmall.utils.CommonUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDao extends BaseDao {

    public int add(Member member) {
        String sql = "INSERT INTO member(email, nick, password, rDatetime) VALUES(?,?,?,?)";
        return executeUpdate(sql,
                member.getEmail(),
                member.getNick(),
                CommonUtil.MD5(member.getPassword()),
                new Timestamp(System.currentTimeMillis()));
    }

    public Member validate(String email, String password) {
        String sql = "SELECT * FROM member WHERE email=? AND password=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, CommonUtil.MD5(password));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setId(rs.getInt("id"));
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                member.setPassword(rs.getString("password"));
                member.setrDatetime(rs.getTimestamp("rDatetime"));
                return member;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public Member findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setId(rs.getInt("id"));
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                member.setPassword(rs.getString("password"));
                member.setrDatetime(rs.getTimestamp("rDatetime"));
                return member;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public Member findById(Integer id) {
        String sql = "SELECT * FROM member WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setId(rs.getInt("id"));
                member.setEmail(rs.getString("email"));
                member.setNick(rs.getString("nick"));
                member.setPassword(rs.getString("password"));
                member.setrDatetime(rs.getTimestamp("rDatetime"));
                return member;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    public int update(Member member) {
        String sql = "UPDATE member SET nick=?, password=? WHERE id=?";
        return executeUpdate(sql,
                member.getNick(),
                CommonUtil.MD5(member.getPassword()),
                member.getId());
    }

    public int updatePassword(Integer memberId, String newPassword) {
        String sql = "UPDATE member SET password=? WHERE id=?";
        return executeUpdate(sql, CommonUtil.MD5(newPassword), memberId);
    }

    public int resetPassword(String email, String password) {
        String sql = "UPDATE member SET password=? WHERE email=?";
        return executeUpdate(sql, CommonUtil.MD5(password), email);
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM member WHERE id=?";
        return executeUpdate(sql, id);
    }
}