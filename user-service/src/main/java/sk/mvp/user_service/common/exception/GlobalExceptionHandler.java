package sk.mvp.user_service.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.common.exception.data.QErrorResponse;
import sk.mvp.user_service.common.exception.data.QError;
import sk.mvp.user_service.common.filter.LoggingFilter;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    //handle authorization JWT token exceptions
    // 1. CHYBY TOKENU (Expirácia, podpis, formát) -> 401 Unauthorized
    @ExceptionHandler({
            ExpiredJwtException.class,
            SignatureException.class,
            MalformedJwtException.class,
            UnsupportedJwtException.class,
    })
    public ResponseEntity<QErrorResponse> handleAuthenticationExceptions(Exception e, HttpServletRequest request) {
        log.warn("Authentication failed: {}", e.getMessage());
        return createQErrorResponse(ErrorType.TOKEN_INVALID, e.getMessage(), request.getRequestURI(), null);
    }

    // handle custom application exceptions
    @ExceptionHandler(QApplicationException.class)
    public ResponseEntity<QErrorResponse> handleCustomAplicationException(QApplicationException ex, HttpServletRequest request) {
        return createQErrorResponse(ex.getErrorType(), ex.getMessage(), request.getRequestURI(), ex.getData());
    }

    // handle custom application exceptions
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<QErrorResponse> handleInvalidInputRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return createQErrorResponse(ErrorType.INPUT_VALIDATION_ERROR, ex.getMessage(), request.getRequestURI(), null);
    }

    // handle default runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<QErrorResponse> handleOtherRuntime(RuntimeException ex, HttpServletRequest request) {
        log.error(ErrorType.INTERNAL_SERVER_ERROR.toString(), ex);
        return createQErrorResponse(ErrorType.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<QErrorResponse> handleOptimisticLockingFailure(OptimisticLockException ex, HttpServletRequest request) {
        return createQErrorResponse(ErrorType.DATA_CONCURRENT_MODIFICATION, null, request.getRequestURI(), null);
    }

    // Java DTO validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<QErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, Object> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return createQErrorResponse(ErrorType.INPUT_VALIDATION_ERROR,
                "Input Validation exception",
                request.getRequestURI(),
                fieldErrors);

    }

    //TODO: remove message paramter

    private ResponseEntity<QErrorResponse> createQErrorResponse(ErrorType errorType, String message, String path,
                                                                Map<String, Object> data) {
        QError QError = new QError(errorType, path, data);
        if (message != null) {
            QError.setMessage(message);
        }

        return new ResponseEntity<>(new QErrorResponse(QError), new HttpHeaders(), QError.getStatusCode());
    }
}
