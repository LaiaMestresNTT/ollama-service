package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.kafka.ScraperProducer;
import com.alertbotspring.ollamaconsumer.model.DataDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class GorqExtractionService {
    private final ChatClient chatClient;

    public GorqExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    private final String SYSTEM_PROMPT = "You are a strict JSON data extractor. Your output must be ONLY a valid JSON object, no extra text.\n\n" +
            "A product request is any message where the user mentions wanting, searching, or looking for a product to buy.\n\n" +
            "If it IS a product request, return:\n" +
            "{\"name\": \"<product>\", \"brand\": \"<brand>\", \"price\": \"<number or no especificado>\", \"rating\": \"<number or no especificado>\", \"action\": \"buscar_producto\"}\n\n" +
            "If it is NOT a product request, return:\n" +
            "{\"action\": \"no_aplicable\"}\n\n" +
            "EXAMPLES:\n" +
            "User: Buenas bot estoy buscando un ordenador Samsung de 600€ con valoración 3\n" +
            "Output: {\"name\": \"ordenador\", \"brand\": \"Samsung\", \"price\": \"600\", \"rating\": \"3\", \"action\": \"buscar_producto\"}\n\n" +
            "User: Buenas bot quiero unos auriculares Apple\n" +
            "Output: {\"name\": \"auriculares\", \"brand\": \"Apple\", \"price\": \"no especificado\", \"rating\": \"no especificado\", \"action\": \"buscar_producto\"}\n\n" +
            "User: I'm looking for a Samsung fridge under 800€\n" +
            "Output: {\"name\": \"fridge\", \"brand\": \"Samsung\", \"price\": \"800\", \"rating\": \"no especificado\", \"action\": \"buscar_producto\"}\n\n" +
            "User: Hola, ¿cómo estás?\n" +
            "Output: {\"action\": \"no_aplicable\"}\n\n";

    public DataDTO processMessage(String message) {
        var converter = new BeanOutputConverter<>(DataDTO.class);

        String formatInstructions = converter.getFormat();

        String respuesta = this.chatClient.prompt()
                .system(s -> s.text(SYSTEM_PROMPT))
                .user(u -> u.text(message + "\n\n" + formatInstructions))
                .call()
                .content();

        return converter.convert(respuesta);
    }

}
