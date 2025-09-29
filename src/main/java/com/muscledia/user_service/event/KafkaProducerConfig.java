package com.muscledia.user_service.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Kafka configuration for user service event publishing
 */
@Configuration
@ConditionalOnProperty(value = "kafka.events.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    // ===========================================
    // TOPIC CREATION
    // ===========================================

    @Bean
    public NewTopic userEventsTopic() {
        return new NewTopic(userEventsTopic, 3, (short) 1)
                .configs(Map.of(
                        "retention.ms", "604800000", // 7 days
                        "cleanup.policy", "delete"
                ));
    }

    // ===========================================
    // PRODUCER CONFIGURATION
    // ===========================================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Basic Configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability Configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicates
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Performance Configuration
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batches
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait up to 10ms for batching
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // Timeout Configuration
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // JSON Serialization
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        log.info("Kafka producer configured with bootstrap servers: {}", bootstrapServers);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(userEventsTopic);

        // Add error handling
        template.setProducerListener(new org.springframework.kafka.support.LoggingProducerListener<>());

        return template;
    }
}
