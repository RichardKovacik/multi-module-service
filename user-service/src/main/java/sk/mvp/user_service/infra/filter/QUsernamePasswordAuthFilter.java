//package sk.mvp.user_service.auth.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.stereotype.Component;
//import sk.mvp.user_service.auth.dto.LoginReq;
//import sk.mvp.user_service.common.constants.AuthConts;
//import sk.mvp.user_service.common.exception.AccountLockedExp;
//import sk.mvp.user_service.common.exception.auth.InvalidInputDataException;
//import sk.mvp.user_service.common.http.CachedHttpServletRequest;
//import sk.mvp.user_service.common.reddis.IRedisService;
//
//import java.io.IOException;
//
//
//public class QUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {
//    private final ObjectMapper objectMapper;
//    private IRedisService redisService;
//
//    public QUsernamePasswordAuthFilter(AuthenticationManager authenticationManager,
//                                       AuthenticationSuccessHandler successHandler,
//                                       AuthenticationFailureHandler failureHandler,
//                                       ObjectMapper objectMapper,
//                                       IRedisService redisService) {
//        this.objectMapper = objectMapper;
//        this.redisService = redisService;
//        setAuthenticationManager(authenticationManager);
//        setAuthenticationSuccessHandler(successHandler);
//        setAuthenticationFailureHandler(failureHandler);
//        setFilterProcessesUrl("/api/auth/web/login");
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//
//        LoginReq loginReq;
//        try {
//            CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(request);
//            loginReq = objectMapper.readValue(cachedHttpServletRequest.getInputStream(), LoginReq.class);
//            // first check if user is not in blacklist (temporary locked cause of too meany login attempts)
//            String lockedUserKey = AuthConts.REDISS_AUTH_BLACKLIST_USER_COLL + loginReq.username();
//            if (redisService.has(lockedUserKey)) {
//                throw new AccountLockedExp("User account has been locked because of too many failed login");
//            }
//
//            request.setAttribute("username", loginReq.username());
//        }catch (IOException e){
//            throw new InvalidInputDataException("Bad Login Request data");
//        }
//        // TODO: do validiation encapsulated inside logi req DTO
//        //Manuálna validácia formátu,
//        if (loginReq.username() == null || loginReq.username().isEmpty()
//                || loginReq.password() == null || loginReq.password().isEmpty()) {
//            // Vyhodíme špeciálnu výnimku, ktorú spracuje Failure Handler
//            throw new InvalidInputDataException("Bad Login Request");
//        }
//
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(
//                        loginReq.username(),
//                        loginReq.password()
//                );
//
//        return getAuthenticationManager().authenticate(authToken);
//    }
//}
