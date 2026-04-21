package com.example.shoppingmall.service;

import com.example.shoppingmall.dao.AddressDao;
import com.example.shoppingmall.dao.AreaDao;
import com.example.shoppingmall.model.ShipAddress;
import com.example.shoppingmall.model.ShipArea;
import com.example.shoppingmall.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AddressService {
    private AddressDao addressDao = new AddressDao();
    private AreaDao areaDao = new AreaDao();

    /**
     * 从Token获取用户ID
     */
    private Integer getMemberIdFromToken(HttpServletRequest request) {
        String token = JwtUtil.getTokenFromRequest(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        return JwtUtil.getMemberIdFromToken(token);
    }

    public Map<String, Object> addAddress(ShipAddress address, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        address.setMemberId(memberId);

        // 保存是否需要设置默认地址的标志
        boolean shouldSetDefault = (address.getIsDefault() != null && address.getIsDefault() == 1);

        // 插入时总是设为非默认，稍后再处理
        address.setIsDefault(0);

        // 插入地址并获取生成的ID
        Integer newAddressId = addressDao.add(address);

        if (newAddressId != null && newAddressId > 0) {
            // 如果需要设置默认地址
            if (shouldSetDefault) {
                // 使用新生成的addressId调用setDefaultAddress
                int defaultResult = addressDao.setDefaultAddress(memberId, newAddressId);
                if (defaultResult > 0) {
                    result.put("code", 1);
                    result.put("message", "地址添加并设置为默认地址成功");
                    result.put("addressId", newAddressId);
                } else {
                    result.put("code", 1);
                    result.put("message", "地址添加成功，但设置默认地址失败");
                    result.put("addressId", newAddressId);
                }
            } else {
                result.put("code", 1);
                result.put("message", "地址添加成功");
                result.put("addressId", newAddressId);
            }
        } else {
            result.put("code", -2);
            result.put("message", "地址添加失败");
        }

        return result;
    }

    public Map<String, Object> updateAddress(ShipAddress address, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress oldAddress = addressDao.findById(address.getId());
        if (oldAddress == null || !oldAddress.getMemberId().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "地址不存在或无权修改");
            return result;
        }

        address.setMemberId(memberId);

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressDao.setDefaultAddress(memberId, address.getId());
        }

        int updateResult = addressDao.update(address);
        if (updateResult > 0) {
            result.put("code", 1);
            result.put("message", "地址更新成功");
        } else {
            result.put("code", -3);
            result.put("message", "地址更新失败");
        }

        return result;
    }

    public Map<String, Object> deleteAddress(Integer addressId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress address = addressDao.findById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "地址不存在或无权删除");
            return result;
        }

        int deleteResult = addressDao.delete(addressId);
        if (deleteResult > 0) {
            result.put("code", 1);
            result.put("message", "地址删除成功");
        } else {
            result.put("code", -3);
            result.put("message", "地址删除失败");
        }

        return result;
    }

    public Map<String, Object> getAddressList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        List<ShipAddress> addressList = addressDao.findList(memberId);

        result.put("code", 1);
        result.put("list", addressList);

        return result;
    }

    public Map<String, Object> getAddressDetail(Integer addressId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress address = addressDao.findById(addressId);

        if (address != null && address.getMemberId().equals(memberId)) {
            result.put("code", 1);
            result.put("address", address);
        } else {
            result.put("code", -2);
            result.put("message", "地址不存在或无权查看");
        }

        return result;
    }

    public Map<String, Object> setDefaultAddress(Integer addressId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress address = addressDao.findById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            result.put("code", -2);
            result.put("message", "地址不存在或无权设置");
            return result;
        }

        int setResult = addressDao.setDefaultAddress(memberId, addressId);
        if (setResult > 0) {
            result.put("code", 1);
            result.put("message", "设置默认地址成功");
        } else {
            result.put("code", -3);
            result.put("message", "设置默认地址失败");
        }

        return result;
    }

    public Map<String, Object> getDefaultAddress(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer memberId = getMemberIdFromToken(request);
        if (memberId == null) {
            result.put("code", -1);
            result.put("message", "请先登录");
            return result;
        }

        ShipAddress address = addressDao.getDefaultAddress(memberId);

        if (address != null) {
            result.put("code", 1);
            result.put("address", address);
        } else {
            result.put("code", 0);
            result.put("message", "暂无默认地址");
        }

        return result;
    }

    public Map<String, Object> getAreaList() {
        Map<String, Object> result = new HashMap<>();
        List<ShipArea> areaList = areaDao.findList();

        result.put("code", 1);
        result.put("list", areaList);

        return result;
    }
}