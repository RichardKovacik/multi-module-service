package sk.mvp.multiservice.notifyservice.apiClients.exception;

public class ClientException extends RuntimeException {
    // Throw this for permanent bugs (like HTTP 401 Unauthorized or HTTP 400 Bad Requests)
    public ClientException(String message) {
        super(message);
    }
}
