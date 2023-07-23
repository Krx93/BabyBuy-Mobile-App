package com.example.androidstudio;

public class Upload {
    private String name;
    private String description;
    private String price;
    private String url;
    private String id;

    public Upload() {
        // Default constructor required for calls to DataSnapshot.getValue(Upload.class)
    }

    public Upload(String name, String description, String price, String url, String id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.url = url;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
