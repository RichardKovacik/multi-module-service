package sk.mvp.multiservice.notifyservice.sandbox;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

//@Component
@Slf4j
public class ResilienceContextDiagnostic implements CommandLineRunner {

    private final ApplicationContext context;
    private final RetryRegistry retryRegistry;

    public ResilienceContextDiagnostic(ApplicationContext context, RetryRegistry retryRegistry) {
        this.context = context;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public void run(String... args) {
        log.info("=== STARTING SPRING RESILIENCE DIAGNOSTIC CONTEXT LOG ===");

        // 1. Verify if Resilience4j parsed your properties instance name
        boolean retryConfigExists = retryRegistry.getAllRetries()
                .stream()
                .anyMatch(r -> r.getName().equals("resendEmailRetry"));
        log.info("Diagnostic - Resilience4j Registry contains 'resendEmailRetry': {}", retryConfigExists);

        // 2. Identify the exact bean type registered within the application context container
        try {
            Object clientBean = context.getBean("resendApiClientImpl");
            log.info("Diagnostic - Exact Spring Context Class: {}", clientBean.getClass().getName());
            log.info("Diagnostic - Is Bean a Proxy: {}", java.lang.reflect.Proxy.isProxyClass(clientBean.getClass()) || clientBean.getClass().getName().contains("$$SpringCGLIB"));
        } catch (Exception e) {
            log.error("Diagnostic - Failed to fetch resendApiClient from context by name: {}", e.getMessage());
        }

        // 3. Output all beans containing 'resend' to check for duplicates
        String[] beans = context.getBeanNamesForType(Object.class);
        Arrays.stream(beans)
                .filter(name -> name.toLowerCase().contains("resend"))
                .forEach(name -> log.info("Diagnostic - Found tracking bean registered name: [{}] -> type: [{}]",
                        name, context.getType(name).getName()));

        log.info("=== END OF RESILIENCE DIAGNOSTIC CONTEXT LOG ===");
    }
}
