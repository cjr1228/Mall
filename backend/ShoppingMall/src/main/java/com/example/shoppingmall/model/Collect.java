package com.example.shoppingmall.model;

import java.util.Date;

public class Collect {
    private Integer id;
    private Integer memberid;
    private Member member;
    private String goodsNo;
    private Goods goods;
    private Date colTime;

    public Collect() {}

    public Collect(Integer memberid, String goodsNo) {
        this.memberid = memberid;
        this.goodsNo = goodsNo;
        this.colTime = new Date();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMemberid() { return memberid; }
    public void setMemberid(Integer memberid) { this.memberid = memberid; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public String getGoodsNo() { return goodsNo; }
    public void setGoodsNo(String goodsNo) { this.goodsNo = goodsNo; }

    public Goods getGoods() { return goods; }
    public void setGoods(Goods goods) { this.goods = goods; }

    public Date getColTime() { return colTime; }
    public void setColTime(Date colTime) { this.colTime = colTime; }
}