package com.alertbotspring.ollamaconsumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_requests")
public class ProductRequest {

    @Id
    @Field("request_id")
    private String requestId;

    @Field("user_id")
    private String userId;

    private String name;
    private String brand;
    private String price;
    private String rating;
    private String status;

}
