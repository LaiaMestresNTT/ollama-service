package com.alertbotspring.ollamaconsumer.service;


import com.alertbotspring.ollamaconsumer.model.ScrapedProduct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinalResponseService {

    // Este va a generar la respuesta de vuelta a whatsapp con el TOP 3 de productos

    private final ChatClient chatClient;

    public FinalResponseService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    private static final String SYSTEM_PROMPT =
            "Eres un asistente de compras amigable que ayuda a los usuarios por WhatsApp. " +
                    "Tu tarea es redactar un mensaje natural, breve y personalizado en español recomendando productos de Amazon. " +
                    "El mensaje debe sonar cercano, no robótico. Usa emojis con moderación. " +
                    "Si no hay productos disponibles, comunícalo con amabilidad y sugiere intentar con otros criterios.";


    public String generateRecommendationMessage(List<ScrapedProduct> products) {
        String userContent = buildPrompt(products);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userContent)
                .call()
                .content();
    }

    private String buildPrompt(List<ScrapedProduct> products) {
        if (products == null || products.isEmpty()) {
            return "No encontré ningún producto que cumpla los criterios de búsqueda del usuario. " +
                    "Genera un mensaje amable indicando que no se encontraron resultados y sugiere afinar la búsqueda.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("He encontrado los siguientes productos en Amazon para el usuario. ")
                .append("Genera un mensaje de WhatsApp recomendándolos de forma natural:\n\n");

        for (int i = 0; i < products.size(); i++) {
            ScrapedProduct p = products.get(i);
            sb.append(i + 1).append(". ")
                    .append("Nombre: ").append(p.getName()).append("\n")
                    .append("   Marca: ").append(p.getBrand()).append("\n")
                    .append("   Precio: ").append(p.getPrice()).append("€\n")
                    .append("   Valoración: ").append(p.getRating()).append("/5")
                    .append(" (").append(p.getRatingCount()).append(" reseñas)\n")
                    .append("   Score de calidad: ").append(p.getScore()).append("\n")
                    .append("   Enlace: ").append(p.getURL()).append("\n\n");
        }

        return sb.toString();
    }
}
