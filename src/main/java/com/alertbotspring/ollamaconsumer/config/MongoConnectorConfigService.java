package com.alertbotspring.ollamaconsumer.config;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class MongoConnectorConfigService {

    private final WebClient webClient;
    // URL de Kafka Connect dentro de la red de Docker
    private final String CONNECT_URL = "http://connect:8083/connectors";

    public MongoConnectorConfigService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void setupConnector() {
        String connectorName = "mongo-sink-nlp-results";

        // Definición del JSON de configuración
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
                Map.entry("value.converter.value.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicNameStrategy"),

                // Estrategia de ID: Usar el campo "id" generado como UUID en Java
                Map.entry("document.id.strategy", "com.mongodb.kafka.connect.sink.processor.id.strategy.PartialValueStrategy"),
                Map.entry("document.id.strategy.partial.value.projection.type", "allowlist"),
                Map.entry("document.id.strategy.partial.value.projection.list", "id"),
                Map.entry("writemodel.strategy", "com.mongodb.kafka.connect.sink.writemodel.strategy.ReplaceOneBusinessKeyStrategy")
        );

        Map<String, Object> requestBody = Map.of(
                "name", connectorName,
                "config", config
        );

        // Lógica de registro: Primero intentamos borrar el viejo para actualizar, luego creamos
        webClient.delete()
                .uri(CONNECT_URL + "/" + connectorName)
                .retrieve()
                // Ignoramos el error si el conector no existía previamente
                .onStatus(status -> status.is4xxClientError(), response -> null)
                .toBodilessEntity()
                .subscribe(unused -> {
                    webClient.post()
                            .uri(CONNECT_URL)
                            .bodyValue(requestBody)
                            .retrieve()
                            .toBodilessEntity()
                            .subscribe(
                                    res -> System.out.println("✅ Mongo Sink Connector configurado con éxito."),
                                    err -> System.err.println("❌ Error al configurar el conector: " + err.getMessage())
                            );
                });
    }
}