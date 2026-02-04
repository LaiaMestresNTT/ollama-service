package com.alertbotspring.ollamaconsumer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder (
            @Value("${SPRING_AI_OPENAI_API_KEY}") String apiKey,
            @Value("${SPRING_AI_OPENAI_BASE_URL") String baseUrl) {

        var openAiApi = new OpenAiApi(baseUrl, apiKey);
        var chatModel = new OpenAiChatModel(openAiApi);

        return ChatClient.builder(chatModel);
    }

}
