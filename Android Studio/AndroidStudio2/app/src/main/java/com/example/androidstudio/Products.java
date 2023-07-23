package com.example.androidstudio;

public class Products {

    String name, price, desc, selectedImage;

    public Products(String desc) {

    }

    public Products(String name, String price, String desc) {
        this.name = name;
        this.price = price;
        this.desc = desc;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }



}
