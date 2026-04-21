package com.example.shoppingmall.model;

public class Goods {
    private Integer id;
    private String no;
    private String name;
    private String desc;
    private String img;
    private Float price;
    private Integer qty;
    private Float discount;

    public Goods() {}

    public Goods(String no, String name, Float price, Integer qty) {
        this.no = no;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.discount = 1.0f;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNo() { return no; }
    public void setNo(String no) { this.no = no; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public Float getDiscount() { return discount; }
    public void setDiscount(Float discount) { this.discount = discount; }

    // 计算会员价
    public Float getMemberPrice() {
        if (price != null && discount != null) {
            return price * discount;
        }
        return price;
    }
}