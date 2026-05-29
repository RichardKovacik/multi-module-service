package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.dto.SendGridRequest;

@Slf4j
public class SendGridEmailProviderImpl extends AbstractEmailProvider {
    private final RestClient restClient;
    private final String apiKey;
    private final String host;
    private final String uri;

    public SendGridEmailProviderImpl(SpringTemplateEngine templateEngine, String fromEmail, String fromName, String apiKey, String host, String uri) {
        super(templateEngine, fromEmail, fromName);
        this.apiKey = apiKey;
        this.host = host;
        this.uri = uri;
        this.restClient = RestClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

    }

    @Override
    protected void sendRawEmail(String toEmail, String subject, String htmlContent) {
        log.info("Sending email via SendGrid API to: {}",toEmail);

        SendGridRequest sendGridRequest = SendGridRequest.fromDomain(toEmail, subject, htmlContent, getFromEmail(), getFromName());

        //calling rest client
        //TODO: set timeout max 10 seconds
        ResponseEntity<Void> response = restClient.post()
                .uri(uri)
                .body(sendGridRequest)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Email successfully accepted by SendGrid");
        } else {
            log.error("SendGrid returned failure status code: {}", response.getStatusCode());
            throw new RuntimeException("SendGrid API failed with status: " + response.getStatusCode());
        }

    }
}
