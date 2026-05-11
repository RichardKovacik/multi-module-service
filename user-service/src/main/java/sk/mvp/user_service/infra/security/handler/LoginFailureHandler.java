//package sk.mvp.user_service.auth.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//import sk.mvp.user_service.common.constants.AuthConts;
//import sk.mvp.user_service.common.exception.AccountLockedExp;
//import sk.mvp.user_service.common.exception.auth.InvalidInputDataException;
//import sk.mvp.user_service.common.exception.data.ErrorType;
//import sk.mvp.user_service.common.exception.data.QError;
//import sk.mvp.user_service.common.exception.data.QErrorResponse;
//import sk.mvp.user_service.common.reddis.IRedisService;
//
//import java.io.IOException;
//import java.util.List;
//
////@Component
////@Qualifier("loginFailureHandler")
//public class LoginFailureHandler implements AuthenticationFailureHandler {
//    private IRedisService redisService;
//    private final ObjectMapper objectMapper;
//
//    public LoginFailureHandler(IRedisService redisService, ObjectMapper objectMapper) {
//        this.redisService = redisService;
//        this.objectMapper = objectMapper;
//    }
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//
//        //increase attempt in redis only if badCredential Excp or userNotFound
//        //if actaul atempt +1 >= MAX then lock user, add to redis blacklist
//        if (exception instanceof BadCredentialsException) {
//            String username = (String) request.getAttribute("username");
//
//            String loginAttemptsKey = AuthConts.REDISS_AUTH_LOGIN_ATTEMPTS_USER_COLL + username;
//            String lockedUserKey = AuthConts.REDISS_AUTH_BLACKLIST_USER_COLL + username;
//
//            // exute lua script : increment bad attempt + check if user is locked(reach max attemts of login)
//            Long result = redisService.executeLuaScript("redis/login_attempts.lua",
//                    List.of(loginAttemptsKey,lockedUserKey),
//                    300, AuthConts.MAX_LOGIN_ATTEMPTS,60);
//           // user is locked fo spesific amount of time
//            if (result == -1) {
//                exception = new AccountLockedExp("User account has been locked because of too many failed login");
//            }
//
//        }
//        //custom response bud bad credentials
//        QErrorResponse errorResponse = prepareJsonResponse(exception);
//
//        // 3. Nastavenie HTTP odpovede
//        response.setStatus(errorResponse.getError().getStatusCode());
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        objectMapper.writeValue(response.getWriter(), errorResponse);
//
//    }
//    private QErrorResponse prepareJsonResponse(AuthenticationException exception) {
//        if (exception instanceof BadCredentialsException) {
//            QError qError = new QError(ErrorType.AUTH_INVALID_CREDENTIALS, AuthConts.WEB_LOGIN_API_PATH,null);
//            return new QErrorResponse(qError);
//        }
//        if (exception instanceof DisabledException) {
//            QError qError = new QError(ErrorType.AUTH_USER_DISABLED, AuthConts.WEB_LOGIN_API_PATH,null);
//            return new QErrorResponse(qError);
//        }
//        if (exception instanceof InvalidInputDataException){
//            QError qError = new QError(ErrorType.INPUT_VALIDATION_ERROR, AuthConts.WEB_LOGIN_API_PATH,null);
//            return new QErrorResponse(qError);
//        }
//        if (exception instanceof AccountLockedExp) {
//            QError qError = new QError(ErrorType.TOO_MANY_REQUESTS, AuthConts.WEB_LOGIN_API_PATH,null);
//            return new QErrorResponse(qError);
//        }
//
//
//        //RETURN DEFAULt GENERIC response
//        QError qError = new QError(ErrorType.AUTH_USER_FAILED,AuthConts.WEB_LOGIN_API_PATH,null);
//        return new QErrorResponse(qError);
//
//    }
//}
