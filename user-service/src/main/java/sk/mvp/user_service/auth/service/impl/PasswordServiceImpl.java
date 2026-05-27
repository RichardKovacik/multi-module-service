package sk.mvp.user_service.auth.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.factory.UserEventFactory;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.user_service.auth.dto.request.PasswordResetInitiateReq;
import sk.mvp.user_service.auth.dto.request.PasswordResetSubmitReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.auth.service.IPasswordService;
import sk.mvp.user_service.auth.service.IVerificationTokenService;
import sk.mvp.user_service.auth.service.VerificationLinkService;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.outbox.dto.OutboxTriggerEvent;
import sk.mvp.user_service.outbox.service.IOutBoxService;
import sk.mvp.user_service.user.service.IUserService;

@Service
public class PasswordServiceImpl implements IPasswordService {
    private final IVerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final IOutBoxService outBoxService;
    private final IUserService userService;
    private final UserEventFactory userEventFactory;
    private final VerificationLinkService verificationLinkService;
    private final ApplicationEventPublisher eventPublisher;

    //TODO: create separte const class + config class
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    @Value("${kafka.user.event.topic}")
    private String userEventTopicName;
    //user service no repsoitro

    public PasswordServiceImpl(IVerificationTokenService verificationTokenService, PasswordEncoder passwordEncoder, IOutBoxService outBoxService, IUserService userService, UserEventFactory userEventFactory, VerificationLinkService verificationLinkService, ApplicationEventPublisher eventPublisher) {
        this.verificationTokenService = verificationTokenService;
        this.passwordEncoder = passwordEncoder;
        this.outBoxService = outBoxService;
        this.userService = userService;
        this.userEventFactory = userEventFactory;
        this.verificationLinkService = verificationLinkService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetInitiateReq request) {
        //check if user with email exists in db, if not exists exception is thrown
        User user = userService.getUserByEmail(request.email());
        //create new VerficationToken spesicifs type using factory and save to db
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
        //generate link
        String link = verificationLinkService.generateVerificationUrl(VerificationTokenType.PASSWORD_RESET, verificationToken.getToken());

        // create password reset event
        BaseEvent<PasswordResetPayload> passwordResetRequestedEvent = this.userEventFactory.createPasswordResetRequestedEvent(
                request.email(),
                link,
                user.getId().toString(),
                MDC.get(CORRELATION_ID_HEADER),
                userEventTopicName);
        //save event to Outbox repo
        outBoxService.saveOutbox(passwordResetRequestedEvent);
        //trigger asynch send only after sucessfull commit transaction
        eventPublisher.publishEvent(new OutboxTriggerEvent(passwordResetRequestedEvent.eventId()));

    }

    @Override
    @Transactional
    public void passwordResetTokenValidate(String token) {
        if (token.isBlank() || token.isEmpty()) {
            throw new IllegalArgumentException("Reset password token is empty");
        }
        //if something goes worng, exception is thrown
        verificationTokenService.getValidVerificationToken(token, VerificationTokenType.PASSWORD_RESET);

    }

    @Override
    @Transactional
    public void verifyAndUpdatePasswordResetToken(PasswordResetSubmitReq request) {
        VerificationToken validToken = verificationTokenService.getValidVerificationToken(request.token(), VerificationTokenType.PASSWORD_RESET);
        //set token to used
        validToken.setUsed(true);
        //set new hashed password
        User user = validToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}
