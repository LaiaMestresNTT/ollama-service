package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.kafka.ScraperProducer;
import com.alertbotspring.ollamaconsumer.model.ExtractedData;
import com.alertbotspring.ollamaconsumer.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtractionRouterService {

    private final OllamaModelService ollamaModelService;
    private final ChatHistoryManager historyManager;
    private final ScraperProducer scraperProducer;
    private final ObjectMapper objectMapper;

    public ExtractionRouterService(OllamaModelService modelService, ChatHistoryManager historyManager, ScraperProducer scraperProducer, ObjectMapper objectMapper) {
        this.ollamaModelService = modelService;
        this.historyManager = historyManager;
        this.scraperProducer = scraperProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Procesa un mensaje entrante, clasifica la intención y enruta la acción.
     *
     * @param chatId     ID del chat de WhatsApp.
     * @param userPrompt Mensaje recibido del usuario.
     */
    public void processIncomingMessage(String chatId, String userPrompt) {

        List<Message> history = historyManager.getHistory(chatId);
        Message userMessage = new Message("user", userPrompt);
        history.add(userMessage);

        System.out.println("Historial:  " + userMessage);

        try {
            // 1. Llamar a Llama para obtener el JSON de extracción/clasificación
            String jsonContent = ollamaModelService.callLlamaApi(history);
            System.out.println("JSON extraído: " + jsonContent);

            // 2. Parsear el JSON
            String cleanedJsonContent = jsonContent.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode rootNode = objectMapper.readTree(cleanedJsonContent);
            System.out.println("JSON parseado al formato: " + rootNode);

            // 3. Routing basado en la intención
            if (rootNode.has("accion") && "no_aplicable".equals(rootNode.get("accion").asText())) {
               // NO RESPONDER Y BORRAR EL HISTORIAL
                history.remove(history.size() - 1);

            } else {

                String TOPIC = "nlp_results";

                // Enviar a Kafka para iniciar el proceso de scraping
                ExtractedData extractedData = objectMapper.treeToValue(rootNode, ExtractedData.class);
                System.out.println("Hemos llegado: " + extractedData);
                scraperProducer.sendMessage(TOPIC, extractedData, chatId);

                // Mensaje fijo de confirmación para el usuario
                String confirmationMessage = "¡Perfecto! Hemos recibido tu solicitud. Estoy buscando el producto y te notificaré con la mejor opción tan pronto como la tenga.";

                // Añadir el mensaje de confirmación al historial
                historyManager.addAssistantMessage(chatId, confirmationMessage);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Error de Parsing LLM (JSON inválido): " + e.getMessage());
            // Si falla el JSON, revertir el historial (quitar el userPrompt)
            history.remove(history.size() - 1);
        } catch (RuntimeException e) {
            System.err.println("Error de comunicación con LLM: " + e.getMessage());
            history.remove(history.size() - 1);
        }
    }
}
