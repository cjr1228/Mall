package com.example.shoppingmall.controller;



import com.example.shoppingmall.model.ShipAddress;
import com.example.shoppingmall.service.AddressService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/address/*")
public class AddressController extends BaseController {
    private AddressService addressService = new AddressService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) pathInfo = "";

            switch (pathInfo) {
                case "/add":
                    addAddress(request, response);
                    break;
                case "/update":
                    updateAddress(request, response);
                    break;
                case "/delete":
                    deleteAddress(request, response);
                    break;
                case "/setDefault":
                    setDefaultAddress(request, response);
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
                case "/list":
                    getAddressList(request, response);
                    break;
                case "/detail":
                    getAddressDetail(request, response);
                    break;
                case "/default":
                    getDefaultAddress(request, response);
                    break;
                case "/areas":
                    getAreaList(request, response);
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

    private void addAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = getParameter(request, "name", "");
        Integer areald = getIntParameter(request, "areald", 1);
        String address = getParameter(request, "address", "");
        String zipcode = getParameter(request, "zipcode", "");
        String telno = getParameter(request, "telno", "");
        Integer isDefault = getIntParameter(request, "isDefault", 0);

        if (name.isEmpty() || address.isEmpty() || telno.isEmpty()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "姓名、地址和电话为必填项");
            sendJsonResponse(response, result);
            return;
        }

        ShipAddress shipAddress = new ShipAddress();
        shipAddress.setName(name);
        shipAddress.setAreald(areald);
        shipAddress.setAddress(address);
        shipAddress.setZipcode(zipcode);
        shipAddress.setTelno(telno);
        shipAddress.setIsDefault(isDefault);

        Map<String, Object> result = addressService.addAddress(shipAddress, request);
        sendJsonResponse(response, result);
    }

    private void updateAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = getIntParameter(request, "id", null);
        String name = getParameter(request, "name", "");
        Integer areald = getIntParameter(request, "areald", 1);
        String address = getParameter(request, "address", "");
        String zipcode = getParameter(request, "zipcode", "");
        String telno = getParameter(request, "telno", "");
        Integer isDefault = getIntParameter(request, "isDefault", 0);

        if (id == null || name.isEmpty() || address.isEmpty() || telno.isEmpty()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数不完整");
            sendJsonResponse(response, result);
            return;
        }

        ShipAddress shipAddress = new ShipAddress();
        shipAddress.setId(id);
        shipAddress.setName(name);
        shipAddress.setAreald(areald);
        shipAddress.setAddress(address);
        shipAddress.setZipcode(zipcode);
        shipAddress.setTelno(telno);
        shipAddress.setIsDefault(isDefault);

        Map<String, Object> result = addressService.updateAddress(shipAddress, request);
        sendJsonResponse(response, result);
    }

    private void deleteAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = getIntParameter(request, "id", null);

        if (id == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误");
            sendJsonResponse(response, result);
            return;
        }

        Map<String, Object> result = addressService.deleteAddress(id, request);
        sendJsonResponse(response, result);
    }

    private void getAddressList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = addressService.getAddressList(request);
        sendJsonResponse(response, result);
    }

    private void getAddressDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = getIntParameter(request, "id", null);

        if (id == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误");
            sendJsonResponse(response, result);
            return;
        }

        Map<String, Object> result = addressService.getAddressDetail(id, request);
        sendJsonResponse(response, result);
    }

    private void setDefaultAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = getIntParameter(request, "id", null);

        if (id == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", -1);
            result.put("message", "参数错误");
            sendJsonResponse(response, result);
            return;
        }

        Map<String, Object> result = addressService.setDefaultAddress(id, request);
        sendJsonResponse(response, result);
    }

    private void getDefaultAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = addressService.getDefaultAddress(request);
        sendJsonResponse(response, result);
    }

    private void getAreaList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = addressService.getAreaList();
        sendJsonResponse(response, result);
    }
}