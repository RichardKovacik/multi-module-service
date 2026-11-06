package sk.mvp.user_service.common.exception.auth;

public class AccountDeletedException extends RuntimeException {
    public AccountDeletedException(String message) {
        super(message);
    }
}
