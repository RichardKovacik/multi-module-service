package sk.mvp.multiservice.notifyservice.apiClients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sk.mvp.multiservice.notifyservice.email.config.SendGridEmailClientProperties;

@Slf4j
@Component
public class SendGridApiClient {
    private RestClient restClient;
    private SendGridEmailClientProperties properties;

    public SendGridApiClient(SendGridEmailClientProperties properties) {
        this.properties = properties;
        restClient = RestClient.create();
    }
}
