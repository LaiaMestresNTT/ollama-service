package com.alertbotspring.ollamaconsumer.service;

import com.alertbotspring.ollamaconsumer.model.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatHistoryManager {

    // Almacenamiento en memoria: Clave (chatId de WhatsApp) -> Valor (Lista de Mensajes)
    private final Map<String, List<Message>> chatHistories = new ConcurrentHashMap<>();

    // Prompt inicial del sistema para dar contexto al LLM
    private static final String SYSTEM_PROMPT = "You are a friendly, concise, and helpful WhatsApp assistant. You respond only based on the conversation context.";

    // Configuración del modelo
    private static final String MODEL_NAME = "llama3.1:8b";

    /**
     * Obtiene el historial completo de un chat, inicializándolo si es la primera vez.
     * @param chatId ID del chat de WhatsApp.
     * @return Lista mutable de mensajes (incluyendo el mensaje de sistema inicial).
     */
    public List<Message> getHistory(String chatId) {
        // computeIfAbsent crea la entrada si no existe, garantizando que siempre hay una lista
        return chatHistories.computeIfAbsent(chatId, k -> {
            List<Message> initialHistory = new ArrayList<>();
            // Añadir el mensaje de sistema como el primer elemento
            initialHistory.add(new Message("system", SYSTEM_PROMPT));
            return initialHistory;
        });
    }

    /**
     * Limpia el historial de un chat específico (similar al comando !reset).
     * @param chatId ID del chat de WhatsApp.
     */
    public void resetHistory(String chatId) {
        chatHistories.remove(chatId);
    }

    /**
     * Obtiene el nombre del modelo.
     */
    public String getModelName() {
        return MODEL_NAME;
    }
}