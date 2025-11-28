package com.alertbotspring.ollamaconsumer.model;

import lombok.Data;

@Data
public class ExtractedData {

    private String product;
    private String level;
    private String price_max;
    private String duration_max;

    public ExtractedData() {
    }

    public ExtractedData(String product, String level, String price_max, String duration_max) {
        this.product = product;
        this.level = level;
        this.price_max = price_max;
        this.duration_max = duration_max;
    }
}

