package com.alertbotspring.ollamaconsumer.model;

import lombok.Data;

@Data
public class ExtractedData {

    private String course;
    private String level;
    private String price_max;
    private String duration_max;
    private String lang;

    public ExtractedData() {
    }

    public ExtractedData(String course, String level, String price_max, String duration_max, String lang) {
        this.course = course;
        this.level = level;
        this.price_max = price_max;
        this.duration_max = duration_max;
        this.lang = lang;
    }
}

