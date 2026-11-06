package sk.mvp.user_service.unit.auth;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.factory.UserEventFactory;
import sk.mvp.common.payloads.AccountRestoredPayload;
import sk.mvp.user_service.auth.dto.request.AccountRestoreReq;
import sk.mvp.user_service.auth.dto.request.AccountRestoreSubmitReq;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.auth.service.impl.VerificationLinkService;
import sk.mvp.user_service.factory.TestDataFactory;
import sk.mvp.user_service.outbox.dto.OutboxTriggerEvent;
import sk.mvp.user_service.outbox.service.IOutBoxService;
import sk.mvp.user_service.auth.dto.request.RegistrationReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.auth.service.IVerificationTokenService;
import sk.mvp.user_service.auth.service.impl.AuthServiceImpl;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.infra.reddis.IRedisService;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.user.entity.UserStatus;
import sk.mvp.user_service.user.repository.UserRepository;
import sk.mvp.user_service.user.repository.VerificationTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private ITokenService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private IRedisService redisService;
    @Mock private IVerificationTokenService verificationTokenService;
    @Mock private UserEventFactory userEventFactory;
    @Mock private IOutBoxService outBoxService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private VerificationLinkService verificationLinkService;
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private final String userEventTopicName = "user-event-topic";

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should throw exception during registration if email is already used")
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {

        //arrange
        RegistrationReq req = TestDataFactory.createRegistrationReq();

        User mockUser = TestDataFactory.createValidUserInstance();
        mockUser.getContact().setEmail(req.getEmail());

        when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        //act + assert
        QApplicationException ex = assertThrows(QApplicationException.class, () -> authService.registerUser(req));
        assertEquals(ErrorType.EMAIL_DUPLICATED, ex.getErrorType());
        // verifies that user save method was never called
        verify(userRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw exception during registration if username is already used")
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        //arrange
        RegistrationReq req = TestDataFactory.createRegistrationReq();

        User mockUser = TestDataFactory.createValidUserInstance();
        mockUser.setUsername(req.getUsername());

        when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        //act + assert
        QApplicationException ex = assertThrows(QApplicationException.class, () -> authService.registerUser(req));
        assertEquals(ErrorType.USERNAME_DUPLICATED, ex.getErrorType());

        // verifies that user save method was never called
        verify(userRepository, never()).save(any());

    }

    @Test
    void confirmEmail_shouldReturnAlreadyVerifiedMessage_WhenUserAlreadyActivated() {

        //arrange
        User user = TestDataFactory.createValidUserInstance();
        user.activate();

        VerificationToken token =
                TestDataFactory.createMockToken(VerificationTokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(30));
        token.setUser(user);

        when(verificationTokenService.getValidVerificationToken(
                token.getToken(),
                VerificationTokenType.EMAIL_VERIFICATION))
                .thenReturn(token);

        //act
        VerificationTokenResponse response =
                authService.confirmEmail(
                        token.getToken());

        //assert
        assertEquals(
                "Email is already verified",
                response.message());

        assertFalse(token.isUsed());
    }

    @Test
    void confirmEmail_shouldReturnVerifiedSuccessMessage_WhenUserInPendingState() {

        //arrange
        User user = TestDataFactory.createValidUserInstance();

        VerificationToken token =
                TestDataFactory.createMockToken(VerificationTokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(30));
        token.setUser(user);

        when(verificationTokenService.getValidVerificationToken(
                token.getToken(),
                VerificationTokenType.EMAIL_VERIFICATION))
                .thenReturn(token);

        //act
        VerificationTokenResponse response =
                authService.confirmEmail(
                        token.getToken());

        //assert
        assertEquals(
                "Email successfully verified",
                response.message());

        assertTrue(token.isUsed());
    }

    @Test
    void intiateAccountRestoreRequest_shouldCreateAccountRestoreRequest_WhenDeletedUserExists() {

        // arrange
        AccountRestoreReq req =
                new AccountRestoreReq("john@test.com");

        User user = TestDataFactory.createValidUserInstance();
        user.remove();
        user.getContact().setEmail(req.email());

        VerificationToken token =
                TestDataFactory.createMockToken(
                        VerificationTokenType.ACCOUNT_RESTORE,
                        Instant.now().plusSeconds(3600)
                );

        BaseEvent<AccountRestoredPayload> event = mock(BaseEvent.class);

        when(userRepository.findByEmail(req.email()))
                .thenReturn(Optional.of(user));

        when(verificationTokenService.createVerificationToken(
                user,
                VerificationTokenType.ACCOUNT_RESTORE))
                .thenReturn(token);

       when(verificationLinkService.generateVerificationUrl(
                VerificationTokenType.ACCOUNT_RESTORE,
                token.getToken()))
                .thenReturn("link");

        when(userEventFactory.createAccountRestoreRequestedEvent(
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                isNull()))
                .thenReturn(event);

        // act
        authService.intiateAccountRestoreRequest(req);

        // assert
        verify(userRepository)
                .findByEmail(req.email());

        verify(verificationTokenService)
                .createVerificationToken(
                        user,
                        VerificationTokenType.ACCOUNT_RESTORE
                );

        verify(outBoxService)
                .saveOutbox(event);

        verify(eventPublisher)
                .publishEvent(any(OutboxTriggerEvent.class));
    }

    @Test
    void intiateAccountRestoreRequest_shouldThrowException_WhenUserIsNotDeleted() {
        AccountRestoreReq req =
                new AccountRestoreReq("john@test.com");

        User user = TestDataFactory.createValidUserInstance();
        user.activate();

        when(userRepository.findByEmail(req.email()))
                .thenReturn(Optional.of(user));

        // act & assert
        QApplicationException ex = assertThrows(QApplicationException.class, () -> authService.intiateAccountRestoreRequest(req));
        assertEquals(ErrorType.AUTH_BAD_REQUEST, ex.getErrorType());
        assertEquals("User account is active", ex.getMessage());

    }

    @Test
    void accuntRestoreSubmit_shouldActivateUserAndMarkTokenAsUsed_WhenRestoreTokenIsValid() {
        // arrange
        String tokenValue = "restore-token";

        // create mock data
        User user = TestDataFactory.createValidUserInstance();
        user.remove();

        VerificationToken verificationToken = TestDataFactory.createMockToken(
                VerificationTokenType.ACCOUNT_RESTORE,
                Instant.now().plusSeconds(3600));
        verificationToken.setUser(user);

        AccountRestoreSubmitReq request =
                new AccountRestoreSubmitReq(tokenValue);

        //apply mock data
        when(verificationTokenService.getValidVerificationToken(
                tokenValue,
                VerificationTokenType.ACCOUNT_RESTORE))
                .thenReturn(verificationToken);

        // act
        authService.accuntRestoreSubmit(request);

        // assert
        verify(verificationTokenService).getValidVerificationToken(
                tokenValue,
                VerificationTokenType.ACCOUNT_RESTORE);

        assertTrue(verificationToken.isUsed());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }




}
