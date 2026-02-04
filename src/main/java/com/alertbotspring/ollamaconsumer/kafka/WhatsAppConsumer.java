package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbotspring.ollamaconsumer.model.DataDTO;
import com.alertbotspring.ollamaconsumer.service.ExtractionRouterService;
import com.alertbot.avro.WhatsAppMessage;
import com.alertbotspring.ollamaconsumer.service.GorqExtractionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppConsumer {

    private final String TOPIC = "whatsapp-in";
    private final String GROUP_ID = "whatsapp-in-group";

    private final ExtractionRouterService routerService;
    private final GorqExtractionService gorqExtractionService;
    private final ScraperProducer scraperProducer;

    public WhatsAppConsumer(ExtractionRouterService routerService, GorqExtractionService gorqExtractionService, ScraperProducer scraperProducer) {
        this.routerService = routerService;
        this.gorqExtractionService = gorqExtractionService;
        this.scraperProducer = scraperProducer;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeWhatsAppIn(WhatsAppMessage whatsAppMessage) {
        String userId = whatsAppMessage.getId().toString();
        String userPrompt = whatsAppMessage.getText().toString();

        if (userId.isEmpty() || userPrompt.isEmpty()) {
            System.out.println("Mensaje de Kafka recibido con userId o texto nulos. Ignorando...");
            return;
        }

        System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": " + whatsAppMessage.getText());

        // LLAMADA AL MODELO
        routerService.processIncomingMessage(userId, userPrompt); // Antigua función
        DataDTO dataDTO = gorqExtractionService.processMessage(userPrompt);

        System.out.println("action: " + dataDTO.action());

        if (!"no_aplicable".equals(dataDTO.action()) || !"no especificado".equals(dataDTO.name())) {
            scraperProducer.sendNewMessage(dataDTO, userId);
        }


    }

}
