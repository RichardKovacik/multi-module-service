package sk.mvp.multiservice.notifyservice.service;

import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;

public class GmailSmtpEmailProviderImpl extends AbstractEmailProvider {
    public GmailSmtpEmailProviderImpl(SpringTemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    protected void sendRawEmail(String toEmail, String subject, String htmlContent) {

    }
}
