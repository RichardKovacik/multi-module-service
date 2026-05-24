package sk.mvp.multiservice.notifyservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.dto.SendGridRequest;

public class SendGridEmailProviderImpl implements IEmailProvider {
    private static final Logger log = LoggerFactory.getLogger(SendGridEmailProviderImpl.class);
    private final RestClient restClient;
    private final SpringTemplateEngine templateEngine;
    private final String fromEmail;
    private final String fromName;
    private final String apiKey;
    private final String host;
    private final String uri;

    public SendGridEmailProviderImpl(String apiKey, String host, String uri, String fromEmail, String fromName, SpringTemplateEngine templateEngine) {
        this.apiKey = apiKey;
        this.host = host;
        this.uri = uri;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.templateEngine = templateEngine;

        this.restClient = RestClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @Async
    public void sendEmail(EmailRequest request) {
            log.info("Sending email via SendGrid API to: {}", request.to());
            // 1. Thymeleaf prepare
            Context thymeleafContext = new Context(java.util.Locale.ENGLISH);
            if (request.templateModel() != null) {
                thymeleafContext.setVariables(request.templateModel());
            }
            String htmlContent = templateEngine.process(request.templateName(), thymeleafContext);

            SendGridRequest sendGridRequest = SendGridRequest.fromDomain(request, htmlContent, fromEmail, fromName);

            //calling rest client
            //TODO: set timeout max 10 seconds
            ResponseEntity<Void> response = restClient.post()
                    .uri(uri)
                    .body(sendGridRequest)
                    .retrieve()
                    .toBodilessEntity();
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email successfully accepted by SendGrid for template: {}", request.templateName());
            } else {
                log.error("SendGrid returned failure status code: {}", response.getStatusCode());
                throw new RuntimeException("SendGrid API failed with status: " + response.getStatusCode());
            }
    }
}
