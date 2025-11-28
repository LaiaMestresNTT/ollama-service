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
    private static final String SYSTEM_PROMPT = "Eres un motor de clasificación y extracción de datos. Tu única tarea es analizar el mensaje del usuario, clasificar su intención y extraer las características del producto, si aplica.\n" +
            "**Debes devolver SIEMPRE un único objeto JSON válido y NADA más.**\n" +
            "El JSON debe contener los siguientes campos OBLIGATORIAMENTE:\n" +
            "1.  **intention** (String): Clasifica el mensaje en uno de estos valores:\n" +
            "    -   'product_search': Si el usuario pide un curso o producto con características.\n" +
            "    -   'conversation': Si es un saludo, despedida, agradecimiento, o cualquier charla general.\n" +
            "    -   'greeting': Si el usuario tiene un problema o pregunta sobre el servicio/bot.\n" +
            "2.  **extracted_data** (Objeto JSON):\n" +
            "    -   Si la 'intention' es 'product_search', debe contener: " +
            "           'product' (ej: 'curso de Python'), " +
            "           'level' (ej: 'principiante'), " +
            "           'duration_max' (ej: '1000 horas')," +
            "           'price_max' (ej: '50€'). Si un valor no se especifica, usa 'no especificado'.\n" +
            "    -   Si la 'intention' NO es 'product_search', debe ser un objeto vacío: {}.\n" +
            "3.  **action** (String): Una breve descripción de la acción interna que debe realizar el sistema (NO una respuesta al usuario).\n" +
            "    -   Si la intención es 'product_search', usa: \"Mandar a Kafka para Scraper.\"\n" +
            "    -   Si la intención NO es 'product_search', usa: \"No requiere acción de scraping.\"";

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