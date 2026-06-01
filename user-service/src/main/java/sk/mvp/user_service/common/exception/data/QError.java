package sk.mvp.user_service.common.exception.data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

public class QError {
    private ErrorType errorType;
    private int statusCode;
    private String message;
    private String path;
    //TODO: zmenit na UTC format
    private Instant timestamp;
    private Map<String, Object> data;

    public QError(ErrorType errorType, String path, Map<String, Object> data) {
        this.errorType = errorType;
        this.statusCode = errorType.getStatus();
        this.path = path;
        this.timestamp = Instant.now();
        this.data = data;
        this.initErrorMessage();
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     *
     * @return error message according error type
     */
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void initErrorMessage() {
        switch (this.errorType){
            case EMAIL_DUPLICATED -> this.message = "Email is already in use.";
            case USERNAME_DUPLICATED -> this.message = "Username is already in use.";
            case TOO_MANY_REQUESTS -> this.message = "You have tried too many times, please try again later.";
            case USER_NOT_FOUND -> this.message = "User not found.";
            case AUTH_INVALID_CREDENTIALS -> this.message = "Invalid username or password.";
            case ROLE_NOT_FOUND -> this.message = "Role not found.";
            case ROLE_ALREADY_ASSIGNED -> this.message = "User has already asigghned requested role.";
            case AUTH_USER_DISABLED -> this.message = "User account is disabled.";
            case AUTH_USER_FAILED -> this.message = "Authentification of user failed";
            case VERIFICATION_TOKEN_INVALID -> this.message = "Verification token is invalid.";
            case VERIFICATION_TOKEN_EXPIRED -> this.message = "Verification token is expired.";
            case DATA_CONCURRENT_MODIFICATION -> this.message = "The data you are trying to update was modified by another user or session";
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
