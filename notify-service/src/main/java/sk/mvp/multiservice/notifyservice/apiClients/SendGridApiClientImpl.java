package sk.mvp.multiservice.notifyservice.apiClients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;
import sk.mvp.multiservice.notifyservice.email.config.SendGridEmailClientProperties;

@Slf4j
@Component
public class SendGridApiClientImpl  implements EmailApiClient{
    private RestClient restClient;
    private SendGridEmailClientProperties properties;

    public SendGridApiClientImpl(SendGridEmailClientProperties properties) {
        this.properties = properties;
        restClient = RestClient.create();
    }

    @Override
    public void executeEmailRequest(EmailApiRequest apiRequest) {
        //execute HTTP request to external API service
    }

    @Override
    public boolean ping() {
        return true;
    }
}
