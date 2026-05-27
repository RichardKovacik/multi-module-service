package sk.mvp.user_service.auth.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import sk.mvp.user_service.auth.entity.VerificationTokenType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.auth.verification-token")
@Getter
@Setter // spring automaticlyy fill up map
public class VerificationTokenConfig {

    @NotEmpty(message = "The token TTL configuration map cannot be empty!")
    private Map<VerificationTokenType, Duration> ttl = new HashMap<>();

    public Duration getTtlFor(VerificationTokenType type) {
        if (!ttl.containsKey(type)) {
            throw new IllegalArgumentException("Missing TTL configuration for token type: " + type);
        }
        return ttl.get(type);
    }
}
