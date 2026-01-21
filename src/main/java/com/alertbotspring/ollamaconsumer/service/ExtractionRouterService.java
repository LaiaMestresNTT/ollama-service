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
     * @param userId     ID del chat de WhatsApp.
     * @param userPrompt Mensaje recibido del usuario.
     */
    public void processIncomingMessage(String userId, String userPrompt) {

        List<Message> history = historyManager.getHistory(userId);
        Message userMessage = new Message("user", userPrompt);

        history.add(userMessage);

        System.out.println("Historial:  " + userMessage);

        try {
            // 1. Llamar a Llama para obtener el JSON de extracción/clasificación
            String jsonContent = ollamaModelService.callLlamaApi(history);
            System.out.println("JSON extraído: " + jsonContent);

            // 2. Parsear + mapear el contenido extraído
            JsonNode rootNode = stringToJson(jsonContent);
            System.out.println("JSON parseado al formato: " + rootNode);

            // 3. Routing basado en la intención
            if (rootNode.has("accion") && "no_aplicable".equals(rootNode.get("accion").asText())) {
               // NO RESPONDER Y BORRAR EL HISTORIAL
                history.remove(history.size() - 1);

            } else {
                // Mapeamos el JSON con la clase ExtractedData
                ExtractedData extractedData = jsonToExtractedData(rootNode, userId);

                // Enviar a Kafka para iniciar el proceso de scraping
                String TOPIC = "nlp_results";
                scraperProducer.sendMessage(TOPIC, extractedData, userId);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Error de Parsing LLM (JSON inválido): " + e.getMessage());
            // Si falla el JSON, revertir el historial (quitar el userPrompt)
            history.remove(history.size() - 1);
        } catch (RuntimeException e) {
            System.err.println("Error de comunicación con LLM: " + e.getMessage());
            // Si falla el modelo, revertir el historial (quitar el userPrompt)
            history.remove(history.size() - 1);
        }
    }

    private JsonNode stringToJson (String jsonContent) throws JsonProcessingException {
        return objectMapper.readTree(extractJson(jsonContent));
    }

    private String extractJson (String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            return "{}";
        }

        // Limpieza básica
        String cleanedJsonContent = rawContent.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // Limpieza con Regex para encontrar '{', '}'
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{.*\\}", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(cleanedJsonContent);

        if (matcher.find()) {
            return matcher.group();
        }

        return cleanedJsonContent; // Si no ha encontrado '{', '}' devuelve la limpieza básica
    }

    private ExtractedData jsonToExtractedData (JsonNode rootNode, String userId) throws JsonProcessingException {
        ExtractedData extractedData = objectMapper.treeToValue(rootNode, ExtractedData.class);
        extractedData.setUser_id(userId);

        return extractedData;
    }
}
