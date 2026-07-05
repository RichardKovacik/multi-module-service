package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.SendGridApiClientImpl;
import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;
import sk.mvp.multiservice.notifyservice.email.config.SendGridEmailClientProperties;

@Slf4j
public class SendGridEmailProviderImpl extends AbstractEmailProvider {
    private SendGridApiClientImpl sendGridApiClientImpl;
    private SendGridEmailClientProperties properties;

    public SendGridEmailProviderImpl(SpringTemplateEngine templateEngine, SendGridApiClientImpl sendGridApiClientImpl, SendGridEmailClientProperties properties) {
        super(templateEngine);
        this.sendGridApiClientImpl = sendGridApiClientImpl;
        this.properties = properties;
    }

    @Override
    protected void sendRawEmail(EmailApiRequest emailApiRequest) {
        log.info("Sending email via SendGrid API to: {}", emailApiRequest.to());

        //SendGridRequest sendGridRequest = SendGridRequest.fromDomain(toEmail, subject, htmlContent, getFromEmail(), getFromName());
        //TODO: call sending post request to SendGrid
    }
}
