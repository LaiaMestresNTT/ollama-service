package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.ExtractedProduct;
import com.alertbotspring.ollamaconsumer.model.DataDTO;
import com.alertbotspring.ollamaconsumer.model.ProductRequest;
import com.alertbotspring.ollamaconsumer.mongo.ProductRequestRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ScraperProducer {

    private final KafkaTemplate<String, ExtractedProduct> kafkaTemplate;
    private final ProductRequestRepository productRequestRepository;

    public ScraperProducer(KafkaTemplate<String, ExtractedProduct> kafkaTemplate, ProductRequestRepository productRequestRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.productRequestRepository = productRequestRepository;
    }

    private final String TOPIC = "nlp_results";


    public void sendNewMessage(DataDTO dataDTO, String userId) {
        // Generamos un ID único aquí para rastrear la petición
        String requestId = UUID.randomUUID().toString();

        // GUARDAR PETICION EN MONGO
        ProductRequest request = new ProductRequest(
                requestId,
                userId,
                handleNull(dataDTO.name()),
                handleNull(dataDTO.brand()),
                handleNull(dataDTO.price()),
                handleNull(dataDTO.rating()),
                "PENDING"
        );

        productRequestRepository.save(request);

        // MONTAR AVRO PARA MANDARLO AL TOPICO
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

