package sk.mvp.multiservice.notifyservice.config.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.service.GmailSmtpEmailProviderImpl;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;
import sk.mvp.multiservice.notifyservice.service.SendGridEmailProviderImpl;

@Configuration
@EnableAsync
public class EmailProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "gmail")
    public IEmailProvider gmailProvider(JavaMailSender mailSender,
                                        SpringTemplateEngine templateEngine,
                                        @Value("${spring.mail.username}") String fromEmail) {
        return new GmailSmtpEmailProviderImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "sendgrid")
    public IEmailProvider sendGridProvider(SpringTemplateEngine templateEngine,
                                           @Value("${app.email.sendgrid.api-key}") String apiKey,
                                           @Value("${app.email.sendgrid.from-email}") String fromEmail,
                                           @Value("${app.email.sendgrid.from-name}") String fromName,
                                           @Value("${app.email.sendgrid.host}") String host,
                                           @Value("${app.email.sendgrid.uri}") String uri) {
        return new SendGridEmailProviderImpl(apiKey, host, uri, fromEmail, fromName, templateEngine);
    }
}
