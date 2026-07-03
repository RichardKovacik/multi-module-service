package sk.mvp.multiservice.notifyservice.email.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.email.sendgrid")
public class SendGridEmailClientProperties {
    @NotBlank(message = "Host base URL must be provided")
    private String host;

    @NotBlank(message = "URI routing path must be defined")
    private String uri;

    @NotBlank(message = "API authentication key must not be blank")
    private String apiKey;

    @NotBlank(message = "Default from email must be provided")
    @Email(message = "Must be a valid email format") // Senior approach: ensures format safety
    private String fromEmail;

    @NotBlank(message = "Default from name must be provided")
    private String fromName;
}
