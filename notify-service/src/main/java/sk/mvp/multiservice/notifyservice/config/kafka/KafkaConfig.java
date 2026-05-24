package sk.mvp.multiservice.notifyservice.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import sk.mvp.common.event.BaseEvent;

@Configuration
public class KafkaConfig {
    @Bean
    public ConsumerFactory<String, BaseEvent> kafkaConsumerFactory(ObjectMapper objectMapper, KafkaProperties config) {
        var props = config.buildConsumerProperties(null);
        var jsonDeserializer = new JsonDeserializer<>(BaseEvent.class,objectMapper); // Použije už nakonfigurovaný mapper
        jsonDeserializer.addTrustedPackages("sk.mvp.common.event");
        jsonDeserializer.setUseTypeHeaders(false);
        var errorDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorDeserializer);

    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, BaseEvent> kafkaConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, BaseEvent>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        return factory;
    }

}