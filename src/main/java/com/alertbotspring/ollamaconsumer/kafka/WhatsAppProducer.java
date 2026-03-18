package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.WhatsAppResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppProducer {

    private final KafkaTemplate<String, WhatsAppResponse> kafkaTemplate;
    private final String TOPIC = "whatsapp-out";

    public WhatsAppProducer(KafkaTemplate<String, WhatsAppResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // manda la respuesta generada por el OllamaFinalResponseService al topic whatsapp-out

    public void sendMessage(String userId, String message) {

        WhatsAppResponse response = WhatsAppResponse.newBuilder()
                .setUserId(userId)
                .setMessage(message)
                .build();

        try {
            kafkaTemplate.send(TOPIC, userId, response);
            System.out.println("🚀 Mensaje enviado a whatsapp-out para userId: " + userId);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar a whatsapp-out: " + e.getMessage());
        }
    }
}
