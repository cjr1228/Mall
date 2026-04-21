package com.example.shoppingmall.model;

public class ShipAddress {
    private Integer id;
    private Integer memberId;
    private Member member;
    private String name;
    private Integer areald;
    private ShipArea shipArea;
    private String address;
    private String zipcode;
    private String telno;
    private Integer isDefault;

    public ShipAddress() {}

    public ShipAddress(Integer memberId, String name, String address, String telno) {
        this.memberId = memberId;
        this.name = name;
        this.address = address;
        this.telno = telno;
        this.isDefault = 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAreald() { return areald; }
    public void setAreald(Integer areald) { this.areald = areald; }

    public ShipArea getShipArea() { return shipArea; }
    public void setShipArea(ShipArea shipArea) { this.shipArea = shipArea; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getTelno() { return telno; }
    public void setTelno(String telno) { this.telno = telno; }

    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
}