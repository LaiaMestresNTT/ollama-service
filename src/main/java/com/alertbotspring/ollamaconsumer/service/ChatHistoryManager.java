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
    // Configuración del modelo
    private static final String MODEL_NAME = "llama3.1:8b";

    // Prompt inicial del sistema para dar contexto al LLM
    private static final String SYSTEM_PROMPT = "Eres un motor de extracción de datos. Tu única tarea es analizar el mensaje del usuario y devolver SIEMPRE un único objeto JSON válido.\n" +
            "1.  Si el mensaje del usuario es una solicitud de un producto devuelve el JSON de extracción (campos: name, brand, price_max, price, rating (de 0.0 a 5.0)). Si hay un campo no especificado complétalo con \"no especificado\"" +
            "2.  Si el mensaje no es una solicitud de producto, devuelve el JSON {\"accion\": \"no_aplicable\"}";


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
     * Añade un mensaje del asistente al historial.
     */
    public void addAssistantMessage(String chatId, String content) {
        List<Message> history = chatHistories.get(chatId);
        if (history != null) {
            history.add(new Message("assistant", content));
        }
    }

    /**
     * Obtiene el nombre del modelo.
     */
    public String getModelName() {
        return MODEL_NAME;
    }
}