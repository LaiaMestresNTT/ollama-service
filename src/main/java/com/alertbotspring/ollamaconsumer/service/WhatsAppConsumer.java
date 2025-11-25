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

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeWhatsAppIn(WhatsAppMessage whatsAppMessage) {

        log.info("✅ Mensaje Avro enviado al topic " + TOPIC + "{}", whatsAppMessage.getText());
    }

}
