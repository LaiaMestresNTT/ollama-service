package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OllamaModelService {

    private final WebClient webClient;
    private final String modelName;

    public OllamaModelService(WebClient webClient, @Value("${ollama.model.name:llama3.1:8b}") String modelName) {
        this.webClient = webClient;
        this.modelName = modelName;
    }

    /**
     * Llamada genérica a la API /api/chat. Devuelve la respuesta como String JSON.
     * @param history Lista de mensajes, incluyendo el SYSTEM_PROMPT.
     * @return El contenido del mensaje del asistente (la cadena JSON).
     */
    public String callLlamaApi(List<Message> history) throws RuntimeException {
        // Construir el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", history,
                "stream", false
        );

        // Llamada síncrona a la API de Ollama
        try {
            JsonNode responseNode = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // Esperamos la respuesta

            String assistantResponse = responseNode.path("message").path("content").asText("");

            if (assistantResponse.isEmpty()) {
                throw new RuntimeException("Respuesta de Llama vacía.");
            }
            System.out.println("Respuesta de la llamada a la API:");
            return assistantResponse;
        } catch (Exception e) {
            throw new RuntimeException("Error en la comunicación con Ollama: " + e.getMessage(), e);
        }
    }
}