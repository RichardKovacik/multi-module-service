package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.dto.ResendEmailApiRequest;

@Slf4j
public class ResendEmailProviderImpl extends AbstractEmailProvider {
    private final RestClient restClient;
    private final String uri;
    private final String apiKey;
    private final String host;

    public ResendEmailProviderImpl(SpringTemplateEngine templateEngine, String fromEmail, String fromName, String uri, String apiKey, String host) {
        super(templateEngine, fromEmail, fromName);
        this.uri = uri;
        this.apiKey = apiKey;
        this.host = host;
        this.restClient = RestClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    @Override
    protected void sendRawEmail(String toEmail, String subject, String htmlContent) {
        //prepare resend request
        String[] to = {toEmail};
        ResendEmailApiRequest resendEmailApiRequest = new ResendEmailApiRequest(getFromEmail(), to, subject, htmlContent);
        //call external resend API to send email
        ResponseEntity<Void> response = restClient.post()
                .uri(uri)
                .body(resendEmailApiRequest)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Email successfully send by Resend provider");
        } else {
            log.error("Resend returned failure status code: {}", response.getStatusCode());
            throw new RuntimeException("Resend API failed with status: " + response.getStatusCode());
        }

    }
}
