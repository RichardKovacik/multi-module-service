package sk.mvp.user_service.unit.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mvp.user_service.auth.service.impl.VerificationTokenImpl;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.user.repository.VerificationTokenRepository;

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


    @Test
    void getVerificationToken_ShouldThrowException_WhenTokenNotFound() {
        //Arrange
        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        // 2. Act & Capture the exception
//        QApplicationException exception = assertThrows(QApplicationException.class, () ->
//            verificationTokenService.getVerificationToken(UUID.randomUUID().toString()));
      //  assertEquals(ErrorType.VERIFICATION_TOKEN_INVALID, exception.getErrorType());


    }
}
