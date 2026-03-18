package com.alertbotspring.ollamaconsumer.kafka;

import com.alertbot.avro.ConfirmScraped;
import com.alertbotspring.ollamaconsumer.model.ScrapedProduct;
import com.alertbotspring.ollamaconsumer.mongo.ScrapedProductRepository;
import com.alertbotspring.ollamaconsumer.service.FinalResponseService;
import com.alertbotspring.ollamaconsumer.service.OllamaModelService;
import com.alertbot.avro.WhatsAppMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinalResultsConsumer {

    private final String TOPIC = "scraping_finished";
    private final String GROUP_ID = "scraping-finished-group";

    private final FinalResponseService finalResponseService;
    private final ScrapedProductRepository repository;
    private final WhatsAppProducer whatsAppProducer;

    public FinalResultsConsumer(FinalResponseService finalResponseService, ScrapedProductRepository repository, WhatsAppProducer whatsAppProducer) {
        this.finalResponseService = finalResponseService;
        this.repository = repository;
        this.whatsAppProducer = whatsAppProducer;
    }

    // consumidor que escucha el topic mlp_result y llamara a OllamaFinalResponse

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void ScrapingFinished(ConfirmScraped confirmScraped) {

        String requestId = confirmScraped.getRequestId().toString();
        String userId = confirmScraped.getUserId().toString();
        int productCount = confirmScraped.getProductCount();

        System.out.println("Scraping_finished recibido | requestId: " + requestId + " | productos: " + productCount);

        // Buscar top 3 productos por score (aunque productCount sea 0, la lista vendrá vacía)
        List<ScrapedProduct> topProducts = repository.findTop3ByRequestIdOrderByScoreDesc(requestId);

        // LLAMADA AL MODELO
        String finalResponseMessage = finalResponseService.generateRecommendationMessage(topProducts);
        System.out.println("Mensaje generado: " + finalResponseMessage);

        // Mandar al topic whatsapp-out
        whatsAppProducer.sendMessage(userId, finalResponseMessage);


    }
}
