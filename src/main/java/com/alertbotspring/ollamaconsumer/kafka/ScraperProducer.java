package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.ExtractedProduct;
import com.alertbotspring.ollamaconsumer.model.DataDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ScraperProducer {

    private final KafkaTemplate<String, ExtractedProduct> kafkaTemplate;

    public ScraperProducer(KafkaTemplate<String, ExtractedProduct> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private final String TOPIC = "nlp_results";


    public void sendNewMessage(DataDTO dataDTO, String userId) {
        // Generamos un ID único aquí para rastrear la petición
        String requestId = UUID.randomUUID().toString();

        ExtractedProduct avroProduct = ExtractedProduct.newBuilder()
                .setRequestId(requestId)
                .setUserId(userId)
                .setName(handleNull(dataDTO.name()))
                .setBrand(handleNull(dataDTO.brand()))
                .setPrice(handleNull(dataDTO.price()))
                .setRating(handleNull(dataDTO.rating()))
                .build();

        try {
            kafkaTemplate.send(TOPIC, userId, avroProduct);
            System.out.println("🚀 Mensaje enviado a Kafka Topic [" + TOPIC + "]: " + avroProduct);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar mensaje a Kafka: " + e.getMessage());
        }

    }

    private String handleNull(Object value) {
        return (value == null) ? "no especificado" : value.toString();
    }
}

