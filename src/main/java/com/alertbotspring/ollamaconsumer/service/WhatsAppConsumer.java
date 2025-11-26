package com.alertbotspring.ollamaconsumer.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alertbot.avro.WhatsAppMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppConsumer {

    private final String TOPIC = "whatsapp-in";
    private final String GROUP_ID = "whatsapp-in-group";
    private static final Logger log = LoggerFactory.getLogger(WhatsAppConsumer.class);

    private final OllamaChatService ollamaChatService;

    public WhatsAppConsumer(OllamaChatService ollamaChatService) {
        this.ollamaChatService = ollamaChatService;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeWhatsAppIn(WhatsAppMessage whatsAppMessage) {
        String chatId = whatsAppMessage.getId().toString();
        String userPrompt = whatsAppMessage.getText().toString();

        if (chatId.isEmpty() || userPrompt.isEmpty()) {
            log.warn("Mensaje de Kafka recibido con chatId o texto nulos. Ignorando.");
            return;
        }

        System.out.println("✅ Mensaje Avro enviado al topic " + TOPIC + ": " + whatsAppMessage.getText());

        // LLAMADA AL MODELO
        String assistantResponse = ollamaChatService.generateResponse(chatId, userPrompt);

        System.out.println("🤖 Respuesta generada para ID: " + chatId + ", respuesta: " + assistantResponse);


    }

}
