package sk.mvp.user_service.integration.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.auth.jwt.JwtProvider;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.integration.BaseIntegrationTest;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.repository.UserRepository;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class UserProfileIT extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private EntityManager entityManager;


    // helper
    private Cookie createAuthCookie(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(),
                user.getTokenVersion(),
                UUID.randomUUID().toString(),
                user.getRolesAsStringWithPrefix());
        return new Cookie("access_token", accessToken);
    }

    private static UserProfile createUpdateDto(String newFirst, String newLast, String newEmail, String newPhone) {
        UserProfile dto = new UserProfile();
        ReflectionTestUtils.setField(dto, "firstName", newFirst);
        ReflectionTestUtils.setField(dto, "lastName", newLast);
        ReflectionTestUtils.setField(dto, "contact", new ContactResp(newEmail, newPhone));
        return dto;
    }


    // TODO: najskor jednoduchy potom parmetrizovany, potom unit testy
    @Transactional
    @ParameterizedTest
    @ValueSource(strings = {"jdoe","fmoore"})
    void shouldReturnUserProfileFromCookie(String username) throws Exception {
        User user = userRepository.findByUsername(username).get();
        Cookie cookie = createAuthCookie(user);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/profile/me").cookie(cookie))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.genderCode").value(user.getGender().getCode()+""))
                .andExpect(jsonPath("$.contact.email").value(user.getContact().getEmail()))
                .andExpect(jsonPath("$.contact.phoneNumber").value(user.getContact().getPhoneNumber()));


    }

    //change profile data test
    @Transactional
    @ParameterizedTest
    @ValueSource(strings = {"jdoe"})
    void shouldChangeUserProfileData(String username) throws Exception {
        User user = userRepository.findByUsername(username).get();
        Cookie cookie = createAuthCookie(user);
        //compose request
        UserProfile req = createUpdateDto("newFirstName", "newLast", "newEmail", "0917547899");
        // DTO to JSON (Jackson ObjectMapper)
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v1/profile/update")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isOk());
        //fetch updated user from db and comapres values from request

        User updatedUser = userRepository.findByUsername(username).get();
        Assertions.assertEquals(req.getFirstName(), updatedUser.getFirstName());
        Assertions.assertEquals(req.getLastName(), updatedUser.getLastName());
        Assertions.assertEquals(req.getContact().email(), updatedUser.getContact().getEmail());
    }

    @Test
    @Transactional
    void shouldFailChangeUserPorfileDataWithExistingEmail() throws Exception {
        User user = userRepository.findByUsername("gtaylor").get();
        Cookie cookie = createAuthCookie(user);

        //compose request
        UserProfile req = createUpdateDto("gtaylor", "newLast", "jdoe@example.com", "0917547899");
        // DTO to JSON (Jackson ObjectMapper)
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v1/profile/update")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.errorType").value(ErrorType.EMAIL_DUPLICATED.toString()));

    }


    @ParameterizedTest
    @ValueSource(strings = {"jdoe","gtaylor"})
    @Transactional
    void shouldFailUpdateProfileWhenOptimisticLockConflictOccurs(String username) throws Exception {
        //arrange
        // old session holds old user data version = 0
        User userFromSessionOne = userRepository.findByUsername(username).get();
        //wee nned to detach first entity A from hibenrate cache
        entityManager.detach(userFromSessionOne);

        // another get from DB beaceod of detacvhed
        // get same entoty but newly fetched from DB version 0
        User userFromSessionTwo = userRepository.findByUsername(username).orElseThrow();

        //acct & assert
        //update user and insrqase version ++ to 1
        userFromSessionTwo.setFirstName("Two newName");
        userRepository.saveAndFlush(userFromSessionTwo);

        //try update obejct A, it cuold fail beacease version is 0, it excperts 1
        // Assert: Pokus o uloženie Objektu A musí zlyhať na Optimistic Lock,
        // pretože v DB je už verzia 1, ale Objekt A posiela verziu 0.
        userFromSessionOne.setFirstName("One NewName");
        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () -> {
            userRepository.saveAndFlush(userFromSessionOne);
        });


    }

}
