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
}
