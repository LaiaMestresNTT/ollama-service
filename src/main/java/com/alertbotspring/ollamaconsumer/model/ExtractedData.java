package com.alertbotspring.ollamaconsumer.model;

import lombok.Data;

@Data
public class ExtractedData {

    private String user_id;
    private String name;
    private String brand;
    private String price;
    private String rating;

    public ExtractedData() {
    }

    public ExtractedData(String user_id, String name, String brand, String price, String rating) {
        this.user_id = user_id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
    }
}

