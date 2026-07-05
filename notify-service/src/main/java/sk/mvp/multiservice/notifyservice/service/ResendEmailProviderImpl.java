package sk.mvp.multiservice.notifyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.ResendApiClientImpl;
import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;
import sk.mvp.multiservice.notifyservice.dto.ResendEmailApiRequest;
import sk.mvp.multiservice.notifyservice.email.config.ResendEmailClientProperties;

@Slf4j
public class ResendEmailProviderImpl extends AbstractEmailProvider {
    private ResendApiClientImpl resendApiClientImpl;
    private ResendEmailClientProperties properties;

    public ResendEmailProviderImpl(SpringTemplateEngine templateEngine,
                                   ResendApiClientImpl resendApiClientImpl,
                                   ResendEmailClientProperties properties) {
        super(templateEngine);
        this.resendApiClientImpl = resendApiClientImpl;
        this.properties = properties;
    }
    @Override
    protected void sendRawEmail(EmailApiRequest emailApiRequest) {
        //call external resend API to send email
       // log.info("Proxy Verification - Class Type: {}", resendApiClientImpl.getClass().getName());
        resendApiClientImpl.executeEmailRequest(emailApiRequest);
    }


}
