package com.alertbotspring.ollamaconsumer.service;


import com.alertbotspring.ollamaconsumer.kafka.WhatsAppProducer;
import com.alertbotspring.ollamaconsumer.model.ScrapedProduct;
import com.alertbotspring.ollamaconsumer.mongo.ScrapedProductRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinalResponseService {

    // Este va a generar la respuesta de vuelta a whatsapp con el TOP 3 de productos

    private final ScrapedProductRepository productRepository;
    private final ChatClient chatClient;
    private final WhatsAppProducer whatsAppProducer;

    public FinalResponseService(ScrapedProductRepository productRepository, ChatClient.Builder chatClientBuilder, WhatsAppProducer whatsAppProducer) {
        this.productRepository = productRepository;
        this.chatClient = chatClientBuilder.build();
        this.whatsAppProducer = whatsAppProducer;
    }

    private static final String SYSTEM_PROMPT = "You are a helpful shopping assistant. " +
            "Given a list of products found on Amazon, write a friendly WhatsApp message in Spanish " +
            "presenting the top results. For each product include: name, price, rating and URL. " +
            "Keep it concise and use emojis. If no products are provided, apologize and suggest trying different search terms.";


    public void generateSendRecommendationMessage(String userId, String requestId, int productCount) {
        String userContent;

        if (productCount > 0) {
            List<ScrapedProduct> products = productRepository.findTop3ByRequestIdOrderByScoreDesc(requestId);
            userContent = buildProductsPrompt(products);
        } else {
            userContent = "No products were found for this search.";
        }

        String responseMessage = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userContent)
                .call()
                .content();
        System.out.println("Mensaje generado: " + responseMessage);

        // Llamar al productor
        whatsAppProducer.sendMessage(userId, responseMessage);
    }

    private String buildProductsPrompt(List<ScrapedProduct> products) {
        StringBuilder sb = new StringBuilder("Here are the products found:\n");
        for (ScrapedProduct p : products) {
            sb.append("- Name: ").append(p.getName()).append("\n");
            sb.append("  Price: ").append(p.getPrice()).append("€\n");
            sb.append("  Rating: ").append(p.getRating()).append("\n");
            sb.append("  URL: ").append(p.getURL()).append("\n\n");
        }
        return sb.toString();
    }
}
