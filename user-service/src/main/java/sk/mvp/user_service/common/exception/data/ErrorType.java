package sk.mvp.user_service.common.exception.data;

public enum ErrorType {
    USER_NOT_FOUND(404),
    AUTH_USER_FAILED(401),
    AUTH_BAD_REQUEST(400),
    AUTH_USER_DISABLED(403),
    AUTH_EMAIL_NOT_VERIFIED(403),
    AUTH_INVALID_CREDENTIALS(401),
    ROLE_NOT_FOUND(404),
    INPUT_VALIDATION_ERROR(400),
    USER_NAME_DUPLICATED(409),
    EMAIL_DUPLICATED(409),
    USERNAME_DUPLICATED(409),
    ROLE_ALREADY_ASSIGNED(409),
    INTERNAL_SERVER_ERROR(500),
    TOKEN_INVALID(401),
    TOKEN_REUSED_DETECTED(409),
    TOO_MANY_REQUESTS(429),
    VERIFICATION_TOKEN_INVALID(401),
    VERIFICATION_TOKEN_EXPIRED(401);

    private int status;

    ErrorType(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
