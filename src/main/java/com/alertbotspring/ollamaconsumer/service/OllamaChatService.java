package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OllamaChatService {

    private final WebClient webClient;
    private final ChatHistoryManager historyManager;

    // Se inyecta la URL de Ollama desde application.properties o variables de entorno
    public OllamaChatService(WebClient.Builder webClientBuilder,
                             @Value("${ollama.api.url}") String ollamaApiUrl,
                             ChatHistoryManager historyManager) {
        this.webClient = webClientBuilder.baseUrl(ollamaApiUrl).build();
        this.historyManager = historyManager;
    }

    /**
     * Llama al endpoint /api/chat de Ollama, manteniendo el historial de la conversación.
     * @param chatId El ID del chat (usado como clave del historial).
     * @param userPrompt El mensaje que el usuario acaba de enviar.
     * @return La respuesta generada por el modelo.
     */
    public String generateResponse(String chatId, String userPrompt) {

        // 1. Obtener el historial actual y añadir el mensaje del usuario
        List<Message> history = historyManager.getHistory(chatId);
        Message userMessage = new Message("user", userPrompt);
        history.add(userMessage);
        System.out.println("Mensaje: " + userMessage);

        try {
            // 2. Construir el cuerpo de la petición con el historial completo
            Map<String, Object> requestBody = Map.of(
                    "model", historyManager.getModelName(),
                    "messages", history,
                    "stream", false,
                    "options", Map.of("num_thread", 4)
            );

            System.out.println("Request body: "+ requestBody);

            // 3. Llamada síncrona a la API de Ollama
            JsonNode responseNode = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // Bloqueamos esperando la respuesta completa

            System.out.println("Llamada a la API");

            // 4. Extraer la respuesta del asistente
            String assistantResponse = responseNode.path("message").path("content").asText("");
            System.out.println("Respuesta asistente: " + assistantResponse);

            if (assistantResponse.isEmpty()) {
                throw new RuntimeException("Respuesta de Llama vacía o en formato inesperado.");
            }

            // 5. Añadir la respuesta del asistente al historial
            history.add(new Message("assistant", assistantResponse));
            System.out.println("Respuesta añadida al historial");

            return assistantResponse;

        } catch (Exception e) {
            System.err.println("Error al comunicarse con Ollama: " + e.getMessage());

            // Revertir el historial si la llamada falla (quitar el mensaje de usuario)
            if (history.size() > 1) { // Asegurarse de no quitar el System Prompt
                history.remove(history.size() - 1);
            }

            return "Disculpa, el servicio LLM no está disponible en este momento.";
        }
    }
}