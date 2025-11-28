package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbotspring.ollamaconsumer.service.FinalResponseService;
import com.alertbotspring.ollamaconsumer.service.OllamaModelService;
import com.alertbot.avro.WhatsAppMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FinalResultsConsumer {

    private final String TOPIC = "mlp_result";
    private final String GROUP_ID = "mlp_result-group";

    /*private final FinalResponseService finalResponseService;

    public FinalResultsConsumer(FinalResponseService finalResponseService) {
        this.finalResponseService = finalResponseService;
    }

    // consumidor que escucha el topic mlp_result y llamara a OllamaFinalResponse

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeMlpResult() {

        // LLAMADA AL MODELO
        //String assistantFinalResponse = finalResponseService.generateResponse(chatId, chatResponse);


    }*/
}
