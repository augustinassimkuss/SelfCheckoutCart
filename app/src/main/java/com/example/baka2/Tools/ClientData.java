package com.example.baka2.Tools;

import java.io.Serializable;

public class ClientData implements Serializable {
    private int id = -1;
    private String full_name;
    private String password;
    private String birth_date;
    private String phone_number;
    private String email;
    private String payment_full_name;
    private String payment_address;
    private String card_number;
    private String expiry_date;
    private String ccv;

    public int getId() { return id; }
    public String getFull_name() { return full_name; }
    public String getPassword() { return password; }
    public String getBirth_date() { return birth_date; }
    public String getPhone_number() { return phone_number; }
    public String getEmail() { return email; }
    public String getPayment_full_name() { return payment_full_name; }
    public String getPayment_address() { return payment_address; }
    public String getCard_number() { return card_number; }
    public String getExpiry_date() { return expiry_date; }
    public String getCcv() { return ccv; }

    public void setId(int id) {this.id = id;}
    public void setFull_name(String full_name) {this.full_name = full_name;}
    public void setPassword(String password) {this.password = password;}
    public void setPhone_number(String phone_number) {this.phone_number = phone_number;}
    public void setBirth_date(String birth_date) {this.birth_date = birth_date;}
    public void setEmail(String email) {this.email = email;}
    public void setPayment_full_name(String payment_full_name) {this.payment_full_name = payment_full_name;}
    public void setPayment_address(String payment_address) {this.payment_address = payment_address;}
    public void setCard_number(String card_number) {this.card_number = card_number;}
    public void setExpiry_date(String expiry_date) {this.expiry_date = expiry_date;}
    public void setCcv(String ccv) {this.ccv = ccv;}


}
