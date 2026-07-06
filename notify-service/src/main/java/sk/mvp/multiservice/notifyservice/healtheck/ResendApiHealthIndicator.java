package sk.mvp.multiservice.notifyservice.healtheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import sk.mvp.multiservice.notifyservice.apiClients.ResendApiClientImpl;

@Component("resendApi")
public class ResendApiHealthIndicator implements HealthIndicator {
    private final ResendApiClientImpl resendApiClient;

    public ResendApiHealthIndicator(ResendApiClientImpl resendApiClient) {
        this.resendApiClient = resendApiClient;
    }

    @Override
    public Health health() {
        ;
        if (resendApiClient.ping()) {
            return Health.up()
                    .withDetail("provider", "Resend External API")
                    .withDetail("status", "ONLINE")
                    .build();
        }
        return Health.down()
                .withDetail("provider", "Resend External API")
                .withDetail("status", "OFFLINE")
                .withDetail("latestExceptionReason", resendApiClient.getLastFault())
                .build();
    }
}
