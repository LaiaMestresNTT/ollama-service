package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbotspring.ollamaconsumer.service.ExtractionRouterService;
import com.alertbot.avro.WhatsAppMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppConsumer {

    private final String TOPIC = "whatsapp-in";
    private final String GROUP_ID = "whatsapp-in-group";

    private final ExtractionRouterService routerService;

    public WhatsAppConsumer(ExtractionRouterService routerService) {
        this.routerService = routerService;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeWhatsAppIn(WhatsAppMessage whatsAppMessage) {
        String chatId = whatsAppMessage.getId().toString();
        String userPrompt = whatsAppMessage.getText().toString();

        if (chatId.isEmpty() || userPrompt.isEmpty()) {
            System.out.println("Mensaje de Kafka recibido con chatId o texto nulos. Ignorando.");
            return;
        }

        System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": " + whatsAppMessage.getText());

        // LLAMADA AL MODELO
        routerService.processIncomingMessage(chatId, userPrompt);


    }

}
