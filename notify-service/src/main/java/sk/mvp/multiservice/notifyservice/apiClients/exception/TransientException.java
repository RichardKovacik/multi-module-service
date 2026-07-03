package sk.mvp.multiservice.notifyservice.apiClients.exception;

public class TransientException extends RuntimeException {
    // Throw this for temporary errors (like HTTP 429 Rate Limits or HTTP 5xx Server Crashes)
    public TransientException(String message) {
        super(message);
    }
}
