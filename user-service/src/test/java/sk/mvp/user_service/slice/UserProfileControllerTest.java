package sk.mvp.user_service.slice;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sk.mvp.user_service.auth.jwt.JwtConfig;
import sk.mvp.user_service.infra.filter.JwtAuthFilter;
import sk.mvp.user_service.auth.jwt.JwtProvider;
import sk.mvp.user_service.user.controller.UserProfileController;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.service.IUserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserProfileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class // Toto zakáže vytvorenie beanu filtra
        ),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
@Import(JwtConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserProfileControllerTest {
    @Autowired
    MockMvc mockMvc; // simulates HTTP requests

    @Autowired
    private JwtConfig jwtConfig;

    @MockBean
    private IUserService userService;

    /**
     * Static factory method to create a UserProfile DTO for testing.
     */
    private static UserProfile createMockProfile(String username, String first, String last, String email) {
        UserProfile profile = new UserProfile();
        // Setting basic fields
        ReflectionTestUtils.setField(profile, "username", username);
        ReflectionTestUtils.setField(profile, "firstName", first);
        ReflectionTestUtils.setField(profile, "lastName", last);
        ReflectionTestUtils.setField(profile, "genderCode", "M");

        // Creating and setting the nested ContactResp object
        ContactResp contact = new ContactResp(email, "+421900123456");
        ReflectionTestUtils.setField(profile, "contact", contact);

        return profile;
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserIsValid() throws Exception {
        String token = "token";
        String username = "username";

        // Create the complex DTO using our factory
        UserProfile mockProfile = createMockProfile(username, "John", "Doe", "john@example.com");

        // 1. Mock the static JwtUtil
        try (MockedStatic<JwtProvider> mockedJwt = mockStatic(JwtProvider.class)) {
            // Mock the internal Claims object
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn(username);

            // Tell JwtUtil to return our mock claims when called with the test token and the key from config
            mockedJwt.when(() -> JwtProvider.parseClaimsFromJwtToken(eq(token), eq(jwtConfig.getAccesKey())))
                    .thenReturn(claims);

            // 2. Mock the Service behavior
            when(userService.getUserByUsername(username)).thenReturn(mockProfile);

            // 3. Perform the request
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/profile/get").cookie(new Cookie("access_token", token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(mockProfile.getUsername()))
                    .andExpect(jsonPath("$.firstName").value(mockProfile.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(mockProfile.getLastName()))
                    .andExpect(jsonPath("$.genderCode").value(mockProfile.getGenderCode()))
                    .andExpect(jsonPath("$.contact.email").value(mockProfile.getContact().email()))
                    .andExpect(jsonPath("$.contact.phoneNumber").value(mockProfile.getContact().phoneNumber()))
                    .andDo(print());
        }



    }
}
