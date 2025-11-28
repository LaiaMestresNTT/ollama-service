package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.kafka.ScraperProducer;
import com.alertbotspring.ollamaconsumer.model.ExtractedData;
import com.alertbotspring.ollamaconsumer.model.LlamaResponseFormat;
import com.alertbotspring.ollamaconsumer.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        System.out.println("Historial: " + userMessage);

        try {
            // 1. Llamar a Llama para obtener el JSON de extracción/clasificación
            String jsonContent = ollamaModelService.callLlamaApi(history);
            System.out.println("JSON extraído: " + jsonContent);

            // 2. Parsear el JSON
            String cleanedJsonContent = jsonContent.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            LlamaResponseFormat llamaResponseFormat = objectMapper.readValue(cleanedJsonContent, LlamaResponseFormat.class);
            System.out.println("JSON parseado a LlamaResponseFormat: " + llamaResponseFormat);

            // 3. Routing basado en la intención
            if ("product_search".equals(llamaResponseFormat.getIntention())) {
                String TOPIC = "nlp_results";

                // Convertir los datos de extracción a JSON para Kafka
                //String extractionDataJson = objectMapper.writeValueAsString(llamaResponseFormat.getExtracted_data());

                // Enviar a Kafka para iniciar el proceso de scraping
                ExtractedData extractedData = llamaResponseFormat.getExtracted_data();
                scraperProducer.sendMessage(TOPIC, extractedData, chatId);

                // Mensaje fijo de confirmación para el usuario
                String confirmationMessage = "¡Perfecto! Hemos recibido tu solicitud. Estoy buscando el producto y te notificaré con la mejor opción tan pronto como la tenga.";

                // Añadir el mensaje de confirmación al historial
                historyManager.addAssistantMessage(chatId, confirmationMessage);

            } else {

                // Nota: Usamos la "action" como placeholder interno para el historial
                historyManager.addAssistantMessage(chatId, llamaResponseFormat.getAction());

                // Devolver cadena vacía: indica a la capa de WhatsApp que NO debe responder
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
