package com.alertbotspring.ollamaconsumer.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private String TOPIC = "whatsapp-out";

    public WhatsAppProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // manda la respuesta generada por el OllamaFinalResponseService al topic whatsapp-out

    public void sendMessage(String userId, String message) {
        try {
            kafkaTemplate.send(TOPIC, userId, message);
            System.out.println("🚀 Mensaje enviado a whatsapp-out para userId: " + userId);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar a whatsapp-out: " + e.getMessage());
        }
    }
}
