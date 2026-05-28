package sk.mvp.multiservice.notifyservice.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // Podpora pre Java 8 Time (Instant, LocalDateTime)
                .registerModule(new JavaTimeModule())
                // Podpora pre zisťovanie mien parametrov v konštruktore (pre Recordy)
                .registerModule(new ParameterNamesModule())
                // Ignoruj neznáme polia (ak pribudnú v novej verzii eventu)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Zapíše Instant ako ISO-8601 String namiesto čísla
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
