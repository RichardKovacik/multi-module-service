package sk.mvp.user_service.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mvp.user_service.auth.entity.VerificationTokenType;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.backend")
@Validated // Ensures all fields are present and valid on startup
@Getter
@Setter
public class BackendUrlConfig {

    @NotBlank(message = "Backend protocol cannot be blank!")
    private String protocol;

    @NotBlank(message = "Backend domain cannot be blank!")
    private String domain;

    @NotBlank(message = "Backend port cannot be blank!")
    private String port;

    @NotEmpty(message = "The token URI configuration map cannot be empty!")
    private Map<VerificationTokenType, @NotBlank(message = "URI path cannot be blank!") String> uri = new HashMap<>();

    public String getUriFor(VerificationTokenType type) {
        if (!uri.containsKey(type)) {
            throw new IllegalArgumentException("Missing URI configuration for token type: " + type);
        }
        return uri.get(type);
    }
//    public String generateConfirmationEmailUrl(String generatedToken) {
//        return UriComponentsBuilder.newInstance()
//                .scheme(protocol)      // http
//                .host(domain)          // localhost
//                .port(port)            // 8081
//                .path(emailConfirmUri) // /api/v1/auth/email/confirm
//                .queryParam("token", generatedToken) // ?token=XYZ
//                .build()
//                .toUriString();
//    }

}

