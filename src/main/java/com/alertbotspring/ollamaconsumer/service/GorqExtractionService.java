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

    private final String SYSTEM_PROMPT = "You are a strict JSON data extractor. Your output must be ONLY a valid JSON object.\n\n" +
            "RULES:\n" +
            "1. If the user is looking for a product, extract:\n" +
            "   - 'name': Product name (e.g., fridge). If not present, 'no especificado'.\n" +
            "   - 'brand': Brand (e.g., Samsung). If not present, 'no especificado'.\n" +
            "   - 'price': Numeric value only. If the user says '300 euros', return 300. If not present, 'null'.\n" +
            "   - 'rating': Numeric value only. If not present, 'null'.\n" +
            "2. If the message is NOT a product request, return exactly: {\"action\": \"no_aplicable\"}\n" +
            "3. NEVER respond with conversational text, ONLY the JSON object.\n\n";


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
