package sk.mvp.user_service.unit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.factory.TestDataFactory;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.user.repository.UserRepository;
import sk.mvp.user_service.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserByUsername_ShouldReturnProfile_WhenUserExists() {
        // Arrange
        User user = TestDataFactory.createValidUserInstance();
        user.setFirstName("John");
        user.setLastName("Doe");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        // Act
        UserProfile result = userService.getUserByUsername("username");

        // Assert
        assertEquals("username", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("M", result.getGenderCode());
        assertEquals(new ContactResp(user.getContact().getEmail(), user.getContact().getPhoneNumber()),
                result.getContact());
        verify(userRepository).findByUsername("username");
    }

    @Test
    void getUserByUsername_ShouldThrowIllegalArgumentException_WhenUsernameIsNull() {
        // Arrange
        String username = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername(username));
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUsername_ShouldThrowIllegalArgumentException_WhenUsernameIsEmpty() {
        // Arrange
        String username = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername(username));
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUsername_ShouldThrowQApplicationException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // Act
        QApplicationException exception = assertThrows(QApplicationException.class,
                () -> userService.getUserByUsername("missing"));

        // Assert
        assertEquals(ErrorType.USER_NOT_FOUND, exception.getErrorType());
        verify(userRepository).findByUsername("missing");
    }

    @Test
    void updateUserProfile_ShouldUpdateProvidedFields_WhenUserExists() {
        // Arrange
        User user = TestDataFactory.createValidUserInstance();
        UserProfile update = new UserProfile();
        update.setFirstName("UpdatedFirst");
        update.setLastName("UpdatedLast");
        update.setContact(new ContactResp("updated@example.com", "555-0123"));
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());

        // Act
        userService.updateUserProfile("username", update);

        // Assert
        assertEquals("UpdatedFirst", user.getFirstName());
        assertEquals("UpdatedLast", user.getLastName());
        assertEquals("updated@example.com", user.getContact().getEmail());
        assertEquals("555-0123", user.getContact().getPhoneNumber());
        verify(userRepository).findByUsername("username");
        verify(userRepository).findByEmail("updated@example.com");
    }

    @Test
    void updateUserProfile_ShouldPreserveExistingValues_WhenFieldsAreNullOrEmpty() {
        // Arrange
        User user = TestDataFactory.createValidUserInstance();
        String originalFirstName = user.getFirstName();
        String originalLastName = user.getLastName();
        String originalEmail = user.getContact().getEmail();
        String originalPhoneNumber = user.getContact().getPhoneNumber();
        UserProfile update = new UserProfile();
        update.setFirstName("");
        update.setLastName(null);
        update.setContact(new ContactResp(originalEmail, ""));
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        // Act
        userService.updateUserProfile("username", update);

        // Assert
        assertEquals(originalFirstName, user.getFirstName());
        assertEquals(originalLastName, user.getLastName());
        assertEquals(originalEmail, user.getContact().getEmail());
        assertEquals(originalPhoneNumber, user.getContact().getPhoneNumber());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void updateUserProfile_ShouldThrowQApplicationException_WhenEmailIsAlreadyInUse() {
        // Arrange
        User user = TestDataFactory.createValidUserInstance();
        User anotherUser = TestDataFactory.createValidUserInstance();
        UserProfile update = new UserProfile();
        update.setContact(new ContactResp("taken@example.com", null));
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(anotherUser));

        // Act
        QApplicationException exception = assertThrows(QApplicationException.class,
                () -> userService.updateUserProfile("username", update));

        // Assert
        assertEquals(ErrorType.EMAIL_DUPLICATED, exception.getErrorType());
        assertNotEquals("taken@example.com", user.getContact().getEmail());
        verify(userRepository).findByEmail("taken@example.com");
    }

    @Test
    void updateUserProfile_ShouldThrowIllegalArgumentException_WhenUsernameIsNull() {
        // Arrange
        UserProfile update = new UserProfile();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile(null, update));
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserProfile_ShouldThrowQApplicationException_WhenUserDoesNotExist() {
        // Arrange
        UserProfile update = new UserProfile();
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // Act
        QApplicationException exception = assertThrows(QApplicationException.class,
                () -> userService.updateUserProfile("missing", update));

        // Assert
        assertEquals(ErrorType.USER_NOT_FOUND, exception.getErrorType());
        verify(userRepository).findByUsername("missing");
        verify(userRepository, never()).findByEmail(anyString());
    }
}
