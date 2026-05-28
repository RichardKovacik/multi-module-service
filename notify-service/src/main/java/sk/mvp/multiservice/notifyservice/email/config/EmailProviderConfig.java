package sk.mvp.multiservice.notifyservice.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.service.GmailSmtpEmailProviderImpl;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;
import sk.mvp.multiservice.notifyservice.service.ResendEmailProviderImpl;
import sk.mvp.multiservice.notifyservice.service.SendGridEmailProviderImpl;

@Configuration
@EnableAsync
@Slf4j
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

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
    public IEmailProvider setResendEmailProvider(SpringTemplateEngine templateEngine,
                                           @Value("${app.email.resend.api-key}") String apiKey,
                                           @Value("${app.email.resend.from-email}") String fromEmail,
                                           @Value("${app.email.resend.from-name}") String fromName,
                                           @Value("${app.email.resend.host}") String host,
                                           @Value("${app.email.resend.uri}") String uri) {
        log.info("====================================================================");
        log.info("ACTIVE EMAIL PROVIDER DETECTED: [ RESEND ] via native RestClient");
        log.info("====================================================================");
        return new ResendEmailProviderImpl(apiKey, host, templateEngine, fromEmail, fromName, uri);
    }
}
