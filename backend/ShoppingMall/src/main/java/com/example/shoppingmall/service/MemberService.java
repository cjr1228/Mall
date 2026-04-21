package com.example.shoppingmall.service;

import com.example.shoppingmall.dao.MemberDao;
import com.example.shoppingmall.model.Member;
import com.example.shoppingmall.utils.CommonUtil;
import com.example.shoppingmall.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class MemberService {
    private MemberDao memberDao = new MemberDao();

    public Map<String, Object> register(Member member) {
        Map<String, Object> result = new HashMap<>();

        if (!CommonUtil.isValidEmail(member.getEmail())) {
            result.put("code", -1);
            result.put("message", "邮箱格式错误");
            return result;
        }

        if (member.getPassword() == null || member.getPassword().length() < 6) {
            result.put("code", -2);
            result.put("message", "密码长度不能少于6位");
            return result;
        }

        Member existing = memberDao.findByEmail(member.getEmail());
        if (existing != null) {
            result.put("code", -3);
            result.put("message", "邮箱已被注册");
            return result;
        }

        if (member.getNick() == null || member.getNick().trim().isEmpty()) {
            String email = member.getEmail();
            int atIndex = email.indexOf("@");
            member.setNick(email.substring(0, atIndex));
        }

        int addResult = memberDao.add(member);
        if (addResult > 0) {
            result.put("code", 1);
            result.put("message", "注册成功");
            result.put("memberId", addResult);
        } else {
            result.put("code", -4);
            result.put("message", "注册失败，请稍后重试");
        }

        return result;
    }

    public Map<String, Object> login(String email, String password) {
        Map<String, Object> result = new HashMap<>();

        if (!CommonUtil.isValidEmail(email)) {
            result.put("code", -1);
            result.put("message", "邮箱格式错误");
            return result;
        }

        if (password == null || password.length() < 6) {
            result.put("code", -2);
            result.put("message", "密码长度不能少于6位");
            return result;
        }

        Member member = memberDao.validate(email, password);
        if (member != null) {
            // 生成Token
            String token = JwtUtil.generateToken(member.getId(), member.getEmail());

            member.setPassword(null);
            result.put("code", 1);
            result.put("message", "登录成功");
            result.put("member", member);
            result.put("token", token); // 返回Token给前端
        } else {
            result.put("code", -3);
            result.put("message", "邮箱或密码错误");
        }

        return result;
    }

    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        // Token方案下，前端直接删除本地Token即可，后端不需要做任何事情
        result.put("code", 1);
        result.put("message", "退出成功");
        return result;
    }

    /**
     * 根据Token获取当前用户
     */
    public Member getCurrentMember(HttpServletRequest request) {
        String token = JwtUtil.getTokenFromRequest(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }

        Integer memberId = JwtUtil.getMemberIdFromToken(token);
        if (memberId == null) {
            return null;
        }

        // 从数据库查询最新用户信息
        return memberDao.findById(memberId);
    }

    public boolean isLogin(HttpServletRequest request) {
        return getCurrentMember(request) != null;
    }

    public Integer getCurrentMemberId(HttpServletRequest request) {
        Member member = getCurrentMember(request);
        return member != null ? member.getId() : null;
    }

    public Map<String, Object> updateProfile(Member member, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (!isLogin(request)) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        Member currentMember = getCurrentMember(request);
        if (!currentMember.getId().equals(member.getId())) {
            result.put("code", -2);
            result.put("message", "权限不足");
            return result;
        }

        int updateResult = memberDao.update(member);
        if (updateResult > 0) {
            result.put("code", 1);
            result.put("message", "更新成功");
        } else {
            result.put("code", -3);
            result.put("message", "更新失败");
        }

        return result;
    }
}