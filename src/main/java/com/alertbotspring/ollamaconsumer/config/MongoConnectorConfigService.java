package com.alertbotspring.ollamaconsumer.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class MongoConnectorConfigService {

    private final WebClient webClient;
    private final String CONNECT_URL = "http://localhost:8083/connectors"; // ollama-service esta fuera de la red de los contenedores

    public MongoConnectorConfigService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Cambiamos @PostConstruct por este listener
    @EventListener(ApplicationReadyEvent.class)
    public void setupConnector() {
        String connectorName = "mongo-sink-nlp-results";

        // IMPORTANTE: Verifica que el campo de ID coincida con tu nuevo AVRO
        Map<String, Object> requestBody = getRequestBody(connectorName);

        // Flujo reactivo con REINTENTOS
        webClient.delete()
                .uri(CONNECT_URL + "/" + connectorName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .toBodilessEntity()
                .flatMap(unused -> webClient.post()
                        .uri(CONNECT_URL)
                        .bodyValue(requestBody)
                        .retrieve()
                        .toBodilessEntity())
                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(10))) // Reintenta 5 veces cada 10 seg
                .subscribe(
                        res -> System.out.println("✅ Conector registrado: " + res.getStatusCode()),
                        err -> {
                            System.err.println("❌ ERROR AL REGISTRAR CONECTOR: " + err.getMessage());
                            err.printStackTrace(); // Esto te dirá si es un Connection Refused
                        }
                );
    }

    private static Map<String, Object> getRequestBody(String connectorName) {
        String idFieldName = "request_id";

        Map<String, Object> config = Map.ofEntries(
                Map.entry("connector.class", "com.mongodb.kafka.connect.MongoSinkConnector"),
                Map.entry("tasks.max", "1"),
                Map.entry("topics", "nlp_results"),
                Map.entry("connection.uri", "mongodb://mongodb_container:27017/?replicaSet=rs0"),
                Map.entry("database", "alertbot_db"),
                Map.entry("collection", "product_requests"),
                Map.entry("key.converter", "org.apache.kafka.connect.storage.StringConverter"),
                Map.entry("value.converter", "io.confluent.connect.avro.AvroConverter"),
                Map.entry("value.converter.schema.registry.url", "http://schema-registry:8081"),
                Map.entry("document.id.strategy", "com.mongodb.kafka.connect.sink.processor.id.strategy.PartialValueStrategy"),
                Map.entry("document.id.strategy.partial.value.projection.type", "allowlist"),
                Map.entry("document.id.strategy.partial.value.projection.list", idFieldName),
                Map.entry("writemodel.strategy", "com.mongodb.kafka.connect.sink.writemodel.strategy.ReplaceOneBusinessKeyStrategy")
        );

        Map<String, Object> requestBody = Map.of("name", connectorName, "config", config);
        return requestBody;
    }
}
