package com.alertbotspring.ollamaconsumer.model;

import lombok.Data;

@Data
public class ExtractedData {

    private String name;
    private String brand;
    private String price;
    private String rating;

    public ExtractedData() {
    }

    public ExtractedData(String name, String brand, String price, String rating) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
    }
}

