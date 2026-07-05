package sk.mvp.multiservice.notifyservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.email.exception.EmailDeliveryException;

import java.util.Map;

@Slf4j
public abstract class AbstractEmailProvider implements IEmailProvider {
    private final SpringTemplateEngine templateEngine;

    public AbstractEmailProvider(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    protected String renderHtmlTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendEmail(EmailRequest request) {
        String htmlContent = renderHtmlTemplate(request.templateName(), request.templateModel());
        EmailApiRequest emailApiRequest = new EmailApiRequest(request.to(), request.subject(), htmlContent);
        sendRawEmail(emailApiRequest);
    }
    //Each child implements its own sending strategy
    protected abstract void sendRawEmail(EmailApiRequest emailApiRequest);

}
