package sk.mvp.user_service.common.exception.auth;


import org.springframework.security.core.AuthenticationException;

public class EmailNotVerifiedException extends AuthenticationException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
