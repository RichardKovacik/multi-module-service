package sk.mvp.user_service.auth.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.factory.UserEventFactory;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.auth.service.VerificationLinkService;
import sk.mvp.user_service.outbox.dto.OutboxTriggerEvent;
import sk.mvp.user_service.outbox.service.IOutBoxService;
import sk.mvp.user_service.auth.config.LoginBruteForceConfig;
import sk.mvp.user_service.auth.dto.request.RegistrationReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;
import sk.mvp.user_service.auth.service.IAuthService;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.auth.dto.response.TokenPair;
import sk.mvp.user_service.auth.dto.request.LoginReq;
import sk.mvp.user_service.auth.service.IVerificationTokenService;
import sk.mvp.user_service.common.exception.AccountLockedExp;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.auth.EmailNotVerifiedException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.infra.reddis.IRedisService;
import sk.mvp.user_service.entity.*;
import sk.mvp.user_service.infra.reddis.RedisCacheKey;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.auth.factory.UserRegistrationFactory;
import sk.mvp.user_service.user.repository.UserRepository;

import java.util.List;

@Service
public class AuthServiceImpl implements IAuthService {
    private ITokenService jwtService;
    private AuthenticationManager authenticationManager;
    //TODO: nelubi sa mi ze tu volam repository priamo
    private UserRepository userRepository;
    private IRedisService redisService;
    private IVerificationTokenService verificationTokenService;
    private UserEventFactory userEventFactory;
    private IOutBoxService outBoxService;
    @Value("${kafka.user.event.topic}")
    private String userEventTopicName;
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private final ApplicationEventPublisher eventPublisher;
    private LoginBruteForceConfig loginBruteForceConfig;
    private UserRegistrationFactory userRegistrationFactory;
    private VerificationLinkService verificationLinkService;

    public AuthServiceImpl(ITokenService jwtService,
                           AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           IRedisService redisService,
                           IVerificationTokenService verificationTokenService,
                           UserEventFactory userEventFactory,
                           IOutBoxService outBoxService,
                           ApplicationEventPublisher eventPublisher,
                           LoginBruteForceConfig loginBruteForceConfig,
                           UserRegistrationFactory userRegistrationFactory,
                           VerificationLinkService verificationLinkService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.redisService = redisService;
        this.verificationTokenService = verificationTokenService;
        this.userEventFactory = userEventFactory;
        this.outBoxService = outBoxService;
        this.eventPublisher = eventPublisher;
        this.userRegistrationFactory = userRegistrationFactory;
        this.loginBruteForceConfig = loginBruteForceConfig;
        this.verificationLinkService = verificationLinkService;
    }

    @Override
    public TokenPair loginUser(LoginReq loginReq) {
        //TODO: upravit do samostantej serivce bruteForce ??
        String loginAttemptsKey = RedisCacheKey.AUTH_LOGIN_ATTEMPTS.getKeyPrefix(loginReq.username());
        String lockedUserKey = RedisCacheKey.AUTH_LOGIN_LOCKOUT.getKeyPrefix(loginReq.username());

        // result = -1 user is locked(reach max attemts of login)
        Long result = redisService.executeLuaScript("redis/login_attempts.lua",
                List.of(loginAttemptsKey, lockedUserKey),
                loginBruteForceConfig.getBruteForceWindow().toSeconds(),
                loginBruteForceConfig.getMaxAttempts(),
                loginBruteForceConfig.getLockoutDuration().toSeconds());

        //if user is locked throw exception
        if (result == -1) {
            throw new QApplicationException("You have tried too many times, please try again later.",
                    ErrorType.TOO_MANY_REQUESTS,
                    null);
        }
        // prebhene autetifikacia najdenie usera, provnanei hesla -> customUserDetailService
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginReq.username(),
                            loginReq.password()
                    )
            );
        }catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new QApplicationException("Invalid username or password", ErrorType.AUTH_INVALID_CREDENTIALS, null);
        }catch (DisabledException e) {
            throw new QApplicationException(e.getMessage(), ErrorType.AUTH_USER_DISABLED, null);
        }catch (AccountLockedExp e) {
            throw new QApplicationException(e.getMessage(), ErrorType.TOO_MANY_REQUESTS, null);
        } catch (EmailNotVerifiedException e){
            throw new QApplicationException(e.getMessage(), ErrorType.AUTH_EMAIL_NOT_VERIFIED, null);
        } catch (AuthenticationException e) {
            throw new QApplicationException(e.getMessage(), ErrorType.AUTH_USER_FAILED, null);
        }
        //success login, remove attempts counter collection in reddis if exists
        redisService.delete(loginAttemptsKey);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return jwtService.generateTokenPair(userDetails);
    }

    @Override
    public TokenPair refreshTokens(String refreshToken) {
        return jwtService.refreshTokens(refreshToken);
    }

    @Override
    @Transactional
    public UserProfile registerUser(RegistrationReq registrationReq) {
        if (registrationReq == null) {
            throw new IllegalArgumentException("Registration user data cannot be null");
        }
        //TODO: 2.check if it is email in good pattern using validator, regex ?? Bean validator
        //1. check if email and username is unique
        isEmailOrUsernameUnique(registrationReq.getEmail(), registrationReq.getUsername());

        // create new instance of user using custom factory method
        User user = userRegistrationFactory.createUnverifiedUser(registrationReq);
        // save user to DB
        User savedUser = userRepository.save(user);
        // save verificationToken to DB
        VerificationToken verificationToken = this.verificationTokenService.createVerificationToken(savedUser, VerificationTokenType.EMAIL_VERIFICATION);
        //Transactionl outbox pattern

        //construct email verification link
        String link = verificationLinkService.generateVerificationUrl(VerificationTokenType.EMAIL_VERIFICATION, verificationToken.getToken());

        // create registrationEvent
        BaseEvent<UserRegisteredPayload> userRegisteredEvent = this.userEventFactory.createUserRegisteredEvent(
                registrationReq.getEmail(),
                link,
                savedUser.getId().toString(),
                MDC.get(CORRELATION_ID_HEADER),
                userEventTopicName);
        //store is in Outbox_events db
        outBoxService.saveOutbox(userRegisteredEvent);
        //produc internal spring event
        eventPublisher.publishEvent(new OutboxTriggerEvent(userRegisteredEvent.eventId()));


        //call asynch method thaht try to put new event in kafka broker
        //asynch runs in separte thread, no blocking of tomcat request thread
        //this.eventProducer.produce(userEventTopicName, registeredEvent);

        return new UserProfile(savedUser);
    }

    @Override
    public void logout(String refreshToken, String accessToken) {
        // remove refresh token from reddis
        jwtService.revokeRefreshToken(refreshToken);
        // add access token to blacklist, acces token could be null
        if (accessToken != null) {
            jwtService.revokeAccessToken(accessToken);
        }

    }

    @Transactional
    @Override
    public VerificationTokenResponse verifyAndUpdateEmailVerificationToken(String verificationToken) {
        VerificationToken foundedToken = verificationTokenService.getValidVerificationToken(verificationToken,
                VerificationTokenType.EMAIL_VERIFICATION);

        User user = foundedToken.getUser();
        if (user.isEmailVerified()) {
            return new VerificationTokenResponse("Email is already verified");
        }
        // hibernate has persistance context, and in the end save updated obejects to db
        foundedToken.setUsed(true);
        user.setEmailVerified(true);

        return new VerificationTokenResponse("Email successfully verified");
    }

//    @Override
//    @Transactional
//    public VerificationTokenResponse verifyAndUpdatePasswordResetVerificationToken(String token, String newPassword) {
//        VerificationToken foundedToken = verificationTokenService.getValidVerificationToken(token);
//        //token is valid continues flow
//        User user = foundedToken.getUser();
//        //update db
//        user.setPassword(passwordEncoder.encode(newPassword));
//        foundedToken.setUsed(true);
//        return new VerificationTokenResponse("Password reset successfully processed");
//    }

    private void isEmailOrUsernameUnique(String email, String username) {
        userRepository.findByEmailOrUsername(email, username).ifPresent(user -> {
            if (user.getContact().getEmail().equalsIgnoreCase(email)) {
                throw new QApplicationException(null, ErrorType.EMAIL_DUPLICATED, null);
            }
            if (user.getUsername().equalsIgnoreCase(username)) {
                throw new QApplicationException(null, ErrorType.USERNAME_DUPLICATED, null);
            }
        });
    }
}
