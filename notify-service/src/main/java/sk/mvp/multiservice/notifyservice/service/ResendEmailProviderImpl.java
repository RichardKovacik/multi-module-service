package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.ResendApiClient;
import sk.mvp.multiservice.notifyservice.dto.ResendEmailApiRequest;
import sk.mvp.multiservice.notifyservice.email.config.ResendEmailClientProperties;

@Slf4j
public class ResendEmailProviderImpl extends AbstractEmailProvider {
    private ResendApiClient resendApiClient;
    private ResendEmailClientProperties properties;

    public ResendEmailProviderImpl(SpringTemplateEngine templateEngine,
                                   ResendApiClient resendApiClient,
                                   ResendEmailClientProperties properties) {
        super(templateEngine);
        this.resendApiClient = resendApiClient;
        this.properties = properties;
    }
    @Override
    protected void sendRawEmail(String toEmail, String subject, String htmlContent) {
        //prepare resend request
        ResendEmailApiRequest resendEmailApiRequest = resendEmailApiRequestPrepare(toEmail, subject, htmlContent);
        //call external resend API to send email
        log.info("Proxy Verification - Class Type: {}", resendApiClient.getClass().getName());
        resendApiClient.initiatePostRequest(resendEmailApiRequest);
    }

    private ResendEmailApiRequest resendEmailApiRequestPrepare(String toEmail, String subject, String htmlContent) {
        String[] to = {toEmail};
        return new ResendEmailApiRequest(properties.getFromEmail(), to, subject, htmlContent);
    }
}
