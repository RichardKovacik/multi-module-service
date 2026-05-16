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
import sk.mvp.common.factory.UserEventFactory;
import sk.mvp.user_service.outbox.service.IOutBoxService;
import sk.mvp.user_service.auth.dto.RegistrationReq;
import sk.mvp.user_service.auth.dto.VerificationTokenResponse;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.auth.service.IVerificationTokenService;
import sk.mvp.user_service.auth.service.impl.AuthServiceImpl;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.infra.reddis.IRedisService;
import sk.mvp.user_service.entity.Contact;
import sk.mvp.user_service.entity.Gender;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.user.repository.UserRepository;

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

    @InjectMocks
    private AuthServiceImpl authService;

    RegistrationReq mockReq;
    Contact mockContact;
    User mockUser;

    @BeforeEach
    void setUp() {
    }

    private VerificationToken createMockToken(boolean verified, boolean used, Instant expiry) {
        User user = new User();
        user.setEmailVerified(verified);

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setUsed(used);
        token.setExpiresAt(expiry);
        token.setToken("tokenUUID");
        return token;
    }


    @Test
    @DisplayName("Should throw exception during registration if user already exists")
    void registerUser_ShouldThrowException_WhenEmailOrUsernameAlreadyExists() {
        mockReq = new RegistrationReq("janko", "pp", "jank@gmail.com", "M");
        mockContact = new Contact(mockReq.getEmail(),"");
        mockUser = new User(mockReq.getUsername(), mockReq.getPassword(), mockContact, Gender.MALE);
        mockUser.setUsername(mockReq.getUsername());
        mockUser.setContact(mockContact);
        mockUser.setId(1);

        //arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        //assert + act
        assertThrows(QApplicationException.class, () -> authService.registerUser(mockReq));

    }

    @Test
    void verifyEmailVerificationToken_ShouldReturnToken_WhenTokenIsValid(){
        //arrange
        VerificationToken mockToken = createMockToken(false , false , Instant.now().plusSeconds(30));
        when(verificationTokenService.getVerificationToken(mockToken.getToken())).thenReturn(mockToken);
        //act
        VerificationTokenResponse result = authService.verifyEmailVerificationToken(mockToken.getToken());
        //assert
        assertTrue(mockToken.isUsed());
        assertTrue(mockToken.getUser().isEmailVerified());
        assertEquals("Email successfully verified", result.message());
    }
    @Test
    void verifyEmailVerificationToken_ShouldReturnCustomExpirationExc_WhenTokenIsExpired(){
        //arrange
        VerificationToken mockToken = createMockToken(false , false , Instant.now().minusSeconds(30));
        when(verificationTokenService.getVerificationToken(mockToken.getToken())).thenReturn(mockToken);
        //act
        // 2. Act & Capture the exception
        QApplicationException exception = assertThrows(QApplicationException.class, () -> {
            authService.verifyEmailVerificationToken(mockToken.getToken());
        });
        //assert
        assertEquals(ErrorType.VERIFICATION_TOKEN_EXPIRED, exception.getErrorType());
    }



//    @Test
//    @DisplayName("Should succesfuly save user")
//    void registerUser_ShouldSaveUser_WhenValidData() {
//        //arrange
//        when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.empty());
//        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
//        when(userRepository.save(any(User.class))).thenReturn(mockUser);
//
//        // Act
//        authService.registerUser(mockReq);
//
//        verify(userRepository, times(1)).save(any(User.class));
//        verify(verificationTokenService, times(1)).createVerificationToken(any(User.class));
//        verify(outBoxService, times(1)).saveOutbox(any());
////        verify(eventPublisher, times(1)).publishEvent(any());
//
//
//    }
}
