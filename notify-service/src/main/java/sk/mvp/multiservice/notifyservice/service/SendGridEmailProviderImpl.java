package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.SendGridApiClient;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.dto.SendGridRequest;
import sk.mvp.multiservice.notifyservice.email.config.ResendEmailClientProperties;
import sk.mvp.multiservice.notifyservice.email.config.SendGridEmailClientProperties;

@Slf4j
public class SendGridEmailProviderImpl extends AbstractEmailProvider {
    private SendGridApiClient sendGridApiClient;
    private SendGridEmailClientProperties properties;

    public SendGridEmailProviderImpl(SpringTemplateEngine templateEngine, SendGridApiClient sendGridApiClient, SendGridEmailClientProperties properties) {
        super(templateEngine);
        this.sendGridApiClient = sendGridApiClient;
        this.properties = properties;
    }

    @Override
    protected void sendRawEmail(String toEmail, String subject, String htmlContent) {
        log.info("Sending email via SendGrid API to: {}",toEmail);

        //SendGridRequest sendGridRequest = SendGridRequest.fromDomain(toEmail, subject, htmlContent, getFromEmail(), getFromName());
        //TODO: call sending post request to SendGrid
    }
}
