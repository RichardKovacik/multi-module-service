package sk.mvp.multiservice.notifyservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.dto.ResendEmailApiRequest;

public class ResendEmailProviderImpl implements IEmailProvider {
    private static final Logger log = LoggerFactory.getLogger(ResendEmailProviderImpl.class);
    private final RestClient restClient;
    private final SpringTemplateEngine templateEngine;
    private final String fromEmail;
    private final String fromName;
    private final String uri;

    public ResendEmailProviderImpl(String apiKey, String host, SpringTemplateEngine templateEngine1, String fromEmail1, String fromName1, String uri1) {
        this.templateEngine = templateEngine1;
        this.fromEmail = fromEmail1;
        this.fromName = fromName1;
        this.uri = uri1;
        this.restClient = RestClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public void sendEmail(EmailRequest request) {
        log.info("Sending email via Resend API to: {}", request.to());
        // 1. Thymeleaf prepare
        Context thymeleafContext = new Context(java.util.Locale.ENGLISH);
        if (request.templateModel() != null) {
            thymeleafContext.setVariables(request.templateModel());
        }
        String htmlContent = templateEngine.process(request.templateName(), thymeleafContext);

        //prepare resend request
        String[] to = {request.to()};
        ResendEmailApiRequest resendEmailApiRequest = new ResendEmailApiRequest(fromEmail, to, request.subject(), htmlContent);

        //call external resend API to send email
        ResponseEntity<Void> response = restClient.post()
                .uri(uri)
                .body(resendEmailApiRequest)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Email successfully send by Resend provider for template: {}", request.templateName());
        } else {
            log.error("Resend returned failure status code: {}", response.getStatusCode());
            throw new RuntimeException("Resend API failed with status: " + response.getStatusCode());
        }


    }
}
