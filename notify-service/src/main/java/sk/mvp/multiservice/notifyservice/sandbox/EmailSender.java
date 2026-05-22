package sk.mvp.multiservice.notifyservice.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;
import sk.mvp.multiservice.notifyservice.service.SendGridEmailProviderImpl;

import java.util.HashMap;
import java.util.Map;

//@Component
@Profile("dev")
public class EmailSender implements CommandLineRunner {

    private final IEmailProvider emailProvider;
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    public EmailSender(IEmailProvider emailProvider) {
        this.emailProvider = emailProvider;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 --- STARTING SENDGRID SANDBOX EMAIL TEST ---");
        String myTestEmail = "richardkovacik.123@gmail.com";
        String testVerificationLink = "link";

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("verificationUrl", testVerificationLink);

        // prepare request
        EmailRequest request = new EmailRequest(
                myTestEmail,
                "🔥 Sandbox Test: Verify Your Email Address",
                "registration-verification",
                templateModel
        );

        try {
            log.info("Sending a real sandbox email to: {}", myTestEmail);
            emailProvider.sendEmail(request);
            log.info("✅ --- SANDBOX EMAIL SENT SUCCESSFULY ---");
        } catch (Exception e) {
            log.error("❌ --- SANDBOX EMAIL TEST FAILED ---", e);
        }




    }
}
