package sk.mvp.multiservice.notifyservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.email.exception.EmailDeliveryException;

import java.util.Map;

@Slf4j
public abstract class AbstractEmailProvider implements IEmailProvider {
    private final SpringTemplateEngine templateEngine;
    @Getter
    private final String fromEmail;
    @Getter
    private final String fromName;

    public AbstractEmailProvider(SpringTemplateEngine templateEngine, String fromEmail, String fromName) {
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    protected String renderHtmlTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    @Async
    public void sendEmail(EmailRequest request) {
        try {
            String htmlContent = renderHtmlTemplate(request.templateName(), request.templateModel());
            sendRawEmail(request.to(), request.subject(), htmlContent);
        } catch (Exception e) {
            log.error("Failed to send email to [{}] using template [{}] via provider [{}]",
                    request.to(), request.templateName(), getClass().getSimpleName(), e);
            throw new EmailDeliveryException("Email sending failed due to: " + e.getMessage(), e);
        }


    }
    //Each child implements its own sending strategy
    protected abstract void sendRawEmail(String toEmail, String subject, String htmlContent);

}
