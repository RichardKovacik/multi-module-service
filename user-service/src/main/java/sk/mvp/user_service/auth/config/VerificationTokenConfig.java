package sk.mvp.user_service.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Getter
public class VerificationTokenConfig {
    @Value("${app.auth.verification-token-email-ttl:P2D}")
    private Duration emailVerifiationTokenTtl;

    @Value("${app.auth.verification-token-password-ttl:PT15M}")
    private Duration passwordVerifiationTokenTtl;
}
