package sk.mvp.user_service.unit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.user.entity.Contact;
import sk.mvp.user_service.user.entity.Gender;
import sk.mvp.user_service.user.entity.Role;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.repository.UserRepository;
import sk.mvp.user_service.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    public static User createDefaultUser(String userName) {
        Contact contact = new Contact();
        contact.setEmail("john.doe@example.com");
        // ... ďalšie polia kontaktu

        User user = new User(
                userName,
                "securePass123",
                contact,
                Gender.MALE
        );

        // Prepojenie oboch strán vzťahu (bi-directional mapping)
        contact.setUser(user);

        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(new HashSet<>(Collections.singletonList(new Role("USER"))));

        return user;
    }

    private static UserProfile createUpdateDto(String newFirst, String newLast, String newEmail, String newPhone) {
        UserProfile dto = new UserProfile();
        ReflectionTestUtils.setField(dto, "firstName", newFirst);
        ReflectionTestUtils.setField(dto, "lastName", newLast);
        ReflectionTestUtils.setField(dto, "contact", new ContactResp(newEmail, newPhone));
        return dto;
    }


    //testing bussines logic of mapping data form repository enityti user -> DTO object profile
    @Test
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        User mockUser = createDefaultUser("johnybee");
        //aaa patern
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        //act
        UserProfile profile = userService.getUserByUsername(mockUser.getUsername());
        //assert
        assertEquals(mockUser.getUsername(), profile.getUsername());
        assertEquals(mockUser.getFirstName(), profile.getFirstName());
        assertEquals(mockUser.getLastName(), profile.getLastName());
        assertEquals(mockUser.getContact().getEmail(), profile.getContact().email());

    }
    @Test
    void getUserByUsername_ShouldReturnException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

       QApplicationException exception =
               assertThrows(QApplicationException.class, () -> userService.getUserByUsername(anyString()));
       assertEquals(exception.getErrorType(), ErrorType.USER_NOT_FOUND);
    }

    @Test
    void updateUserProfile_ShouldUpdateFields_WhenValidDataProvided() {
        UserProfile updateDto = createUpdateDto("JohnNew", "DoeNew", "johnNew.doe@example.com", "77777");
        User mockUser = createDefaultUser("johnybee");

        //arrange
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        //act
        userService.updateUserProfile(mockUser.getUsername(), updateDto);
        //assert
        assertEquals(mockUser.getFirstName(), updateDto.getFirstName());
        assertEquals(mockUser.getLastName(), updateDto.getLastName());
        assertEquals(mockUser.getContact().getEmail(), updateDto.getContact().email());
        assertEquals(mockUser.getContact().getPhoneNumber(), updateDto.getContact().phoneNumber());

        verify(userRepository, times(1)).findByUsername(mockUser.getUsername());


    }
}
