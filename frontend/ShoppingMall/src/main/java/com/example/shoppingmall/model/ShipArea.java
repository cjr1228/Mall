package com.example.shoppingmall.model;

public class ShipArea {
    private Integer id;
    private String area;
    private Float cost;

    public ShipArea() {}

    public ShipArea(String area, Float cost) {
        this.area = area;
        this.cost = cost;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Float getCost() { return cost; }
    public void setCost(Float cost) { this.cost = cost; }
}