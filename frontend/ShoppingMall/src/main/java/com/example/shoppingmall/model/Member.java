package com.example.shoppingmall.model;

import java.util.Date;

public class Member {
    private Integer id;
    private String email;
    private String nick;
    private String password;
    private Date rDatetime;

    public Member() {}

    public Member(String email, String password) {
        this.email = email;
        this.password = password;
        this.rDatetime = new Date();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNick() { return nick; }
    public void setNick(String nick) { this.nick = nick; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getrDatetime() { return rDatetime; }
    public void setrDatetime(Date rDatetime) { this.rDatetime = rDatetime; }
}