package sk.mvp.multiservice.notifyservice.apiClients;

import sk.mvp.multiservice.notifyservice.dto.EmailApiRequest;

public interface EmailApiClient {
   void executeEmailRequest(EmailApiRequest apiRequest);
   boolean ping();
}
