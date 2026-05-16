package sk.mvp.user_service.auth.factory;

import org.springframework.stereotype.Component;
import sk.mvp.user_service.auth.config.VerificationTokenConfig;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.entity.User;

import java.time.Instant;
import java.util.UUID;

@Component
public class VerificationTokenFactory {
    private VerificationTokenConfig verificationTokenConfig;

    VerificationTokenFactory(VerificationTokenConfig verificationTokenConfig) {
        this.verificationTokenConfig = verificationTokenConfig;
    }

    public VerificationToken create(VerificationTokenType tokenType, User user) {
        Instant expiresAt = Instant.now().plus(verificationTokenConfig.getEmailVerifiationTokenTtl());
        return new VerificationToken(UUID.randomUUID().toString(), expiresAt, user, tokenType);

    }
}
