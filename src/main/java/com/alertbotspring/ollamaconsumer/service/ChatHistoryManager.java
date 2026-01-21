package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.model.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatHistoryManager {

    // Almacenamiento en memoria: Clave (userId de WhatsApp) -> Valor (Lista de Mensajes)
    private final Map<String, List<Message>> chatHistories = new ConcurrentHashMap<>();
    // Configuración del modelo
    private static final String MODEL_NAME = "llama3.1:8b";

    // Prompt inicial del sistema para dar contexto al LLM
    private static final String SYSTEM_PROMPT = "You are a strict JSON data extractor. Your output must be ONLY a valid JSON object.\n\n" +
        "RULES:\n" +
        "1. If the user is looking for a product, extract:\n" +
        "   - 'name': Product name (e.g., fridge). If not present, 'no especificado'.\n" +
        "   - 'brand': Brand (e.g., Samsung). If not present, 'no especificado'.\n" +
        "   - 'price': Numeric value only. If the user says '300 euros', return 300. If not present, 'no especificado'.\n" +
        "   - 'rating': Numeric value only. If not present, 'no especificado'.\n" +
        "2. If the message is NOT a product request, return exactly: {\"accion\": \"no_aplicable\"}\n" +
        "3. NEVER respond with conversational text, ONLY the JSON object.\n\n";


    /**
     * Obtiene el historial completo de un chat, inicializándolo si es la primera vez.
     * @param userId ID del chat de WhatsApp.
     * @return Lista mutable de mensajes (incluyendo el mensaje de sistema inicial).
     */
    public List<Message> getHistory(String userId) {
        // Si no existe el historial, lo crea con el System Prompt y lo devuelve
        List<Message> history = chatHistories.computeIfAbsent(userId, k -> {
            List<Message> initialHistory = new ArrayList<>();
            initialHistory.add(new Message("system", SYSTEM_PROMPT));
            return initialHistory;
        });

        // Si ya existe comprobamos si hay que recortar (más de 6 mensajes)
        if (history.size() >= 5) {
            history = trimHistory(userId, history);
            // Volvemos a obtener la lista ya actualizada después del recorte
        }
        return history;
    }

    /**
    * Función para mantener el tamaño del historial
    */

    public List<Message> trimHistory(String userId, List<Message> history) {
        int maxMessages = 4;

        // Mantenemos el SYSTEM_PROMPT (índice 0) y los últimos N mensajes
        Message systemPrompt = history.getFirst();
        List<Message> recentContext = history.subList(history.size() - maxMessages, history.size());

        // Creamos la nueva lista de mensajes
        List<Message> newHistory = new ArrayList<>();
        newHistory.add(systemPrompt);
        newHistory.addAll(recentContext);

        // Añadimos la nueva lista a chatHistories
        chatHistories.put(userId, newHistory);

        return newHistory;
    }

    /**
     * Limpia el historial de un chat específico (similar al comando !reset).
     * @param userId ID del chat de WhatsApp.
     */
    public void resetHistory(String userId) {
        chatHistories.remove(userId);
    }

    /**
     * Añade un mensaje del asistente al historial.
     */
    public void addAssistantMessage(String userId, String content) {
        List<Message> history = chatHistories.get(userId);
        if (history != null) {
            history.add(new Message("assistant", content));
        }
    }

}