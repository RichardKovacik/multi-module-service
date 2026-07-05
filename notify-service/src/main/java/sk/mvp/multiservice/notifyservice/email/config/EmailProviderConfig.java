package sk.mvp.multiservice.notifyservice.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sk.mvp.multiservice.notifyservice.apiClients.ResendApiClientImpl;
import sk.mvp.multiservice.notifyservice.apiClients.SendGridApiClientImpl;
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
                                           SendGridApiClientImpl sendGridApiClientImpl,
                                           SendGridEmailClientProperties properties) {
        log.info("====================================================================");
        log.info("ACTIVE EMAIL PROVIDER DETECTED: [ SendGrid ] via native RestClient");
        log.info("====================================================================");
        return new SendGridEmailProviderImpl(templateEngine, sendGridApiClientImpl, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
    public IEmailProvider setResendEmailProvider(SpringTemplateEngine templateEngine,
                                                 ResendApiClientImpl resendApiClientImpl,
                                                 ResendEmailClientProperties properties) {
        log.info("====================================================================");
        log.info("ACTIVE EMAIL PROVIDER DETECTED: [ RESEND ] via native RestClient");
        log.info("====================================================================");
        return new ResendEmailProviderImpl(templateEngine, resendApiClientImpl, properties);
    }
}
