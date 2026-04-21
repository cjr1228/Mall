package com.example.shoppingmall.model;

import java.util.Date;
import java.util.List;

public class Order {
    private Integer id;
    private String orderNo;
    private Integer memberid;
    private Member member;
    private Integer shipAddressId;
    private ShipAddress shipAddress;
    private String shipType;
    private String payType;
    private Date orderTime;
    private Float amount;
    private Integer status; // 0:未支付, 1:已支付, 2:已取消, 3:已完成
    private List<OrderDetail> orderDetails;

    public Order() {}

    public Order(String orderNo, Integer memberid, Float amount) {
        this.orderNo = orderNo;
        this.memberid = memberid;
        this.amount = amount;
        this.orderTime = new Date();
        this.status = 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getMemberid() { return memberid; }
    public void setMemberid(Integer memberid) { this.memberid = memberid; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Integer getShipAddressId() { return shipAddressId; }
    public void setShipAddressId(Integer shipAddressId) { this.shipAddressId = shipAddressId; }

    public ShipAddress getShipAddress() { return shipAddress; }
    public void setShipAddress(ShipAddress shipAddress) { this.shipAddress = shipAddress; }

    public String getShipType() { return shipType; }
    public void setShipType(String shipType) { this.shipType = shipType; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public Date getOrderTime() { return orderTime; }
    public void setOrderTime(Date orderTime) { this.orderTime = orderTime; }

    public Float getAmount() { return amount; }
    public void setAmount(Float amount) { this.amount = amount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
}