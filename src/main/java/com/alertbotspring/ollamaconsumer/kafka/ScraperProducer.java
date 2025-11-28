package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.ExtractedProduct;
import com.alertbotspring.ollamaconsumer.model.ExtractedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScraperProducer {

    private final KafkaTemplate<String, ExtractedProduct> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ScraperProducer(KafkaTemplate<String, ExtractedProduct> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Manda el producto extraido por OllamaChatService al topico de nlp_results
    public void sendMessage(String TOPIC, ExtractedData extractedData, String chatId) {

        ExtractedProduct avroProduct = ExtractedProduct.newBuilder()
                .setId(chatId)
                .setProduct(extractedData.getProduct())
                .setLevel(extractedData.getLevel())
                .setPriceMax(extractedData.getPrice_max())
                .setDurationMax(extractedData.getDuration_max())
                .build();

        try {
            kafkaTemplate.send(TOPIC, avroProduct);
            System.out.println("🚀 Mensaje enviado a Kafka Topic [" + TOPIC + "]: " + avroProduct);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar mensaje a Kafka: " + e.getMessage());
        }
    }

}

