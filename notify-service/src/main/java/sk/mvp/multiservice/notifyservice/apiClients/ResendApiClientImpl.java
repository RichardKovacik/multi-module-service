package sk.mvp.multiservice.notifyservice.apiClients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sk.mvp.multiservice.notifyservice.apiClients.exception.ClientException;
import sk.mvp.multiservice.notifyservice.apiClients.exception.TransientException;
import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;
import sk.mvp.multiservice.notifyservice.dto.ResendEmailApiRequest;
import sk.mvp.multiservice.notifyservice.email.config.ResendEmailClientProperties;

import java.net.http.HttpClient;

@Slf4j
@Component
public class ResendApiClientImpl implements EmailApiClient{
    private RestClient restClient;
    private ResendEmailClientProperties properties;

    public ResendApiClientImpl(ResendEmailClientProperties properties) {
        this.properties = properties;
        HttpClient javaHttpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectionTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(javaHttpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(properties.getHost())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

    }

    /**
     * Sends the email request out to the Resend cloud platform.
     * Protected by both an automatic retry engine and a protective Circuit Breaker shield.
     */
    @CircuitBreaker(name = "resendEmailCircuitBreaker",fallbackMethod = "fallbackEmail")
    @Retry(name = "resendEmailRetry")
    @Override
    public void executeEmailRequest(EmailApiRequest request) {
        log.info("Calling external API service Resend: {}{}", properties.getHost(), properties.getUri());
        ResendEmailApiRequest resendEmailApiRequest = resendEmailApiRequestPrepare(request);
           restClient.post()
                    .uri(properties.getUri())
                    .body(resendEmailApiRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // Handle rate limiting as a transient exception
                        if (res.getStatusCode().value() == 429) {
                            throw new TransientException("Resend API rate limit triggered (429). Retrying...");
                        }
                        // Handle formatting or auth issues as client exceptions (bypasses retries)
                        throw new ClientException("Fatal validation or key credential error: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new TransientException("Resend system service degradation (5xx). Retrying...");
                    })
                    .toBodilessEntity();

        log.info("[EMAIL SUCCESS] Request accepted by Resend API. Target: {}", request.to());
    }


    public void fallbackEmail(EmailApiRequest request, Throwable exception) {
       log.error("Email failed to send. Reason: {}", exception.getMessage(), exception);
        // Return fallback execution token or log context state to alternate handle
    }


    @Override
    public boolean ping() {
        return true;
    }
    private ResendEmailApiRequest resendEmailApiRequestPrepare(EmailApiRequest request) {
        return new ResendEmailApiRequest(properties.getFromEmail(), new String[]{request.to()}, request.subject(), request.htmlContent());
    }

}
