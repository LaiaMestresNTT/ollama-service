package com.alertbotspring.ollamaconsumer.service;

import com.alertbot.avro.WhatsAppMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppConsumer {

    private final String TOPIC = "whatsapp-in";
    private final String GROUP_ID = "whatsapp-in-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeWhatsAppIn(WhatsAppMessage whatsAppMessage) {
        System.out.println(whatsAppMessage.getText());
    }

}
