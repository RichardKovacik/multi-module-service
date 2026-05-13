package sk.mvp.user_service.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Getter
public class LoginBruteForceConfig {
    /**
     * Time window during which failed login attempts are accumulated.
     * Default: 15 minutes (PT15M)
     */
    @Value("${app.security.brute-force.window:PT15M}")
    private Duration bruteForceWindow;

    /**
     * Lockout duration applied to the user once max login attempts are exceeded.
     * Default: 30 minutes (PT30M)
     */
    @Value("${app.security.brute-force.lockout-duration:PT30M}")
    private Duration lockoutDuration;

    /**
     * Maximum number of allowed failed login attempts before locking out the user.
     * Default: 3 attempts
     */
    @Value("${app.security.brute-force.max-attempts:3}")
    private int maxAttempts;
}
