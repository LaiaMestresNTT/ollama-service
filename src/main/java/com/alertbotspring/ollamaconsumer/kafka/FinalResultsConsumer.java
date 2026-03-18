package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.ConfirmScraped;
import com.alertbotspring.ollamaconsumer.model.ScrapedProduct;
import com.alertbotspring.ollamaconsumer.mongo.ScrapedProductRepository;
import com.alertbotspring.ollamaconsumer.service.FinalResponseService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinalResultsConsumer {

    private final String TOPIC = "scraping_finished";
    private final String GROUP_ID = "scraping-finished-group";

    private final FinalResponseService finalResponseService;

    public FinalResultsConsumer(FinalResponseService finalResponseService) {
        this.finalResponseService = finalResponseService;

    }

    // consumidor que escucha el topic mlp_result y llamara a OllamaFinalResponse

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void ScrapingFinished(ConfirmScraped confirmScraped) {

        String requestId = confirmScraped.getRequestId().toString();
        String userId = confirmScraped.getUserId().toString();
        int productCount = confirmScraped.getProductCount();

        System.out.println("Scraping_finished recibido | requestId: " + requestId + " | productos: " + productCount);

        // LLAMADA AL MODELO
        finalResponseService.generateSendRecommendationMessage(userId, requestId, productCount);


    }
}
