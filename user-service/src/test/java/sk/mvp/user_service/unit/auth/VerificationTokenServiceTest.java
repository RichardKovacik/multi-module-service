package sk.mvp.user_service.unit.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.auth.service.impl.VerificationTokenImpl;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.factory.TestDataFactory;
import sk.mvp.user_service.user.repository.VerificationTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @InjectMocks
    private VerificationTokenImpl verificationTokenService;
    private VerificationToken validToken;


    @BeforeEach
    void setUp() {
        validToken = TestDataFactory.createMockToken(VerificationTokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(30));
    }

    @Test
    void getValidVerificationToken_ShouldReturnToken_WhenTokenIsValid() {
        //Arrange
        when(verificationTokenRepository.findByToken(validToken.getToken())).thenReturn(Optional.of(validToken));
        // 2. Act
        VerificationToken result =
                verificationTokenService.getValidVerificationToken(
                        validToken.getToken(),
                        validToken.getVerificationTokenType());
        //assert
        assertNotNull(result);
        assertEquals(validToken.getToken(), result.getToken());
    }

    @Test
    void getValidVerificationToken_ShouldThrowException_WhenTokenNotFound() {
        //Arrange
        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        // 2. Act
        QApplicationException exception = assertThrows(QApplicationException.class, () ->
            verificationTokenService.getValidVerificationToken(UUID.randomUUID().toString(), VerificationTokenType.EMAIL_VERIFICATION));
        //assert
        assertEquals(ErrorType.VERIFICATION_TOKEN_INVALID, exception.getErrorType());
    }
    @Test
    void getValidVerificationToken_ShouldThrowException_WhenTokenTypeIsInvalid() {
        //Arrange
        when(verificationTokenRepository.findByToken(validToken.getToken())).thenReturn(Optional.of(validToken));
        // 2. Act
        QApplicationException exception = assertThrows(QApplicationException.class, () ->
                verificationTokenService.getValidVerificationToken(validToken.getToken(), VerificationTokenType.ACCOUNT_RESTORE));
        //assert
        assertEquals(ErrorType.VERIFICATION_TOKEN_INVALID, exception.getErrorType());
    }

    @Test
    void getValidVerificationToken_ShouldThrowQException_WhenTokenIsExpired() {
        //Arrange
        validToken.setExpiresAt(Instant.now().minusSeconds(30));
        when(verificationTokenRepository.findByToken(validToken.getToken())).thenReturn(Optional.of(validToken));
        // 2. Act
        QApplicationException exception = assertThrows(QApplicationException.class, () ->
                verificationTokenService.getValidVerificationToken(validToken.getToken(), validToken.getVerificationTokenType()));
        //assert
        assertEquals(ErrorType.VERIFICATION_TOKEN_EXPIRED, exception.getErrorType());
    }

    @Test
    void getValidVerificationToken_ShouldThrowQException_WhenTokenIsAlreadyUsed() {
        //Arrange
        validToken.setUsed(true);

        when(verificationTokenRepository.findByToken(validToken.getToken())).thenReturn(Optional.of(validToken));
        // 2. Act
        QApplicationException exception = assertThrows(QApplicationException.class, () ->
                verificationTokenService.getValidVerificationToken(validToken.getToken(), validToken.getVerificationTokenType()));
        //assert
        assertEquals(ErrorType.VERIFICATION_TOKEN_INVALID, exception.getErrorType());
    }
}
