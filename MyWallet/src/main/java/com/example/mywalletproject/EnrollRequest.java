package com.example.mywalletproject;

public class EnrollRequest {
    private String cnp;
    private String shop_name;
    private String adresa_mac;

    public EnrollRequest(String cnp, String shop_name, String adresa_mac) {
        this.cnp = cnp;
        this.shop_name = shop_name;
        this.adresa_mac = adresa_mac;
    }
}

