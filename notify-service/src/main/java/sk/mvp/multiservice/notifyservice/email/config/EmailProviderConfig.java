package sk.mvp.multiservice.notifyservice.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.ResendApiClient;
import sk.mvp.multiservice.notifyservice.apiClients.SendGridApiClient;
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
        return new GmailSmtpEmailProviderImpl(templateEngine);
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "sendgrid")
    public IEmailProvider sendGridProvider(SpringTemplateEngine templateEngine,
                                           SendGridApiClient sendGridApiClient,
                                           SendGridEmailClientProperties properties) {
        log.info("====================================================================");
        log.info("ACTIVE EMAIL PROVIDER DETECTED: [ SendGrid ] via native RestClient");
        log.info("====================================================================");
        return new SendGridEmailProviderImpl(templateEngine, sendGridApiClient, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
    public IEmailProvider setResendEmailProvider(SpringTemplateEngine templateEngine,
                                                 ResendApiClient resendApiClient,
                                                 ResendEmailClientProperties properties) {
        log.info("====================================================================");
        log.info("ACTIVE EMAIL PROVIDER DETECTED: [ RESEND ] via native RestClient");
        log.info("====================================================================");
        return new ResendEmailProviderImpl(templateEngine,resendApiClient, properties);
    }
}
