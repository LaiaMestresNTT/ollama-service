package com.alertbotspring.ollamaconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LlamaResponseFormat {

    private String intention;
    private ExtractedData extracted_data;

    @JsonProperty("action")
    private String action;

    public LlamaResponseFormat(){}

}
