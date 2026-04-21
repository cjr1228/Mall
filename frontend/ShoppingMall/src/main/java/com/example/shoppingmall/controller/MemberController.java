package com.example.shoppingmall.controller;

import com.example.shoppingmall.model.Member;
import com.example.shoppingmall.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/member/*")
public class MemberController extends BaseController {
    private MemberService memberService = new MemberService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/register":
                    register(request, response);
                    break;
                case "/login":
                    login(request, response);
                    break;
                case "/logout":
                    logout(request, response);
                    break;
                case "/update":
                    updateProfile(request, response);
                    break;
                default:
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("code", 404);
                    result.put("message", "接口不存在");
                    sendJsonResponse(response, result);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/info":
                    getMemberInfo(request, response);
                    break;
                case "/checkLogin":
                    checkLogin(request, response);
                    break;
                default:
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("code", 404);
                    result.put("message", "接口不存在");
                    sendJsonResponse(response, result);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String email = getParameter(request, "email", "");
        String password = getParameter(request, "password", "");
        String nick = getParameter(request, "nick", "");
        String confirmPassword = getParameter(request, "confirmPassword", "");

        if (email.isEmpty() || password.isEmpty()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "邮箱和密码不能为空");
            sendJsonResponse(response, result);
            return;
        }

        if (!password.equals(confirmPassword)) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "两次密码不一致");
            sendJsonResponse(response, result);
            return;
        }

        Member member = new Member();
        member.setEmail(email);
        member.setPassword(password);
        member.setNick(nick);

        Map<String, Object> result = memberService.register(member);
        sendJsonResponse(response, result);
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String email = getParameter(request, "email", "");
        String password = getParameter(request, "password", "");

        // 修改为无request参数的login方法
        Map<String, Object> result = memberService.login(email, password);
        sendJsonResponse(response, result);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = memberService.logout();
        sendJsonResponse(response, result);
    }

    private void getMemberInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = memberService.getCurrentMember(request);
        Map<String, Object> result = new java.util.HashMap<>();

        if (member != null) {
            member.setPassword(null);
            result.put("code", 1);
            result.put("message", "获取成功");
            result.put("member", member);
        } else {
            result.put("code", -1);
            result.put("message", "未登录");
        }

        sendJsonResponse(response, result);
    }

    private void checkLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = memberService.getCurrentMember(request);
        Map<String, Object> result = new java.util.HashMap<>();

        if (member != null) {
            result.put("code", 1);
            result.put("message", "已登录");
            result.put("isLogin", true);
            member.setPassword(null);
            result.put("member", member);
        } else {
            result.put("code", 0);
            result.put("message", "未登录");
            result.put("isLogin", false);
        }

        sendJsonResponse(response, result);
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer memberId = getIntParameter(request, "memberId", null);
        String nick = getParameter(request, "nick", "");
        String password = getParameter(request, "password", "");

        if (memberId == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误");
            sendJsonResponse(response, result);
            return;
        }

        Member member = new Member();
        member.setId(memberId);
        member.setNick(nick);
        if (password != null && !password.isEmpty()) {
            member.setPassword(password);
        }

        Map<String, Object> result = memberService.updateProfile(member, request);
        sendJsonResponse(response, result);
    }
}