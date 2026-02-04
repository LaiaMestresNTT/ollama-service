package com.alertbotspring.ollamaconsumer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GroqConfig {

    @Bean
    @Primary
    public ChatClient.Builder customChatClientBuilder(
            @Value("${SPRING_AI_OPENAI_API_KEY}") String apiKey,
            @Value("${SPRING_AI_OPENAI_BASE_URL}") String baseUrl) {

        var openAiApi = new OpenAiApi(baseUrl, apiKey);
        var chatModel = new OpenAiChatModel(openAiApi);

        return ChatClient.builder(chatModel);
    }

}
