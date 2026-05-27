package sk.mvp.user_service.slice;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sk.mvp.user_service.auth.controller.AuthController;
import sk.mvp.user_service.auth.dto.request.RegistrationReq;
import sk.mvp.user_service.auth.service.IAuthService;
import sk.mvp.user_service.auth.jwt.JwtConfig;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.auth.jwt.JwtAuthFilter;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class // Toto zakáže vytvorenie beanu filtra
        ),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtConfig jwtConfig;

    @MockBean
    private IAuthService authService;

    @Test
    public void createUser_ShouldReturnNewRegisteredUserProfile_WhenRegistrationReqIsValid() throws Exception {
        //Arange
        RegistrationReq mockReq = new RegistrationReq("janko", "paapoooooo", "janko@gmail.com", "M");
        ContactResp contactResp = new ContactResp(mockReq.getEmail(), null);
        UserProfile expectedProfile = new UserProfile();

        expectedProfile.setUsername(mockReq.getUsername());
        expectedProfile.setContact(contactResp);
        expectedProfile.setGenderCode(mockReq.getGenderCode());

        when(authService.registerUser(any(RegistrationReq.class))).thenReturn(expectedProfile);

        //act & assert
        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockReq)))
                .andExpect(status().isOk()) // Testing status
                .andExpect(jsonPath("$.username").value(expectedProfile.getUsername())) // Testing JSON mapping
                .andExpect(jsonPath("$.contact.email").value(expectedProfile.getContact().email()))
                .andDo(print());


    }

    @ParameterizedTest
    @MethodSource("provideInvalidRegistrationRequests")
    public void createUser_ShouldThrowException_WhenRegistrationRequestIsInvalid(RegistrationReq invalidReq) throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andDo(print()) // Uvidíš detaily chyby v logu
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorType").value(ErrorType.INPUT_VALIDATION_ERROR.toString()));
    }

    // Zdroj dát pre test - tu definuj rôzne zlé vstupy
    private static Stream<RegistrationReq> provideInvalidRegistrationRequests() {
        return Stream.of(
                new RegistrationReq("a", "heslo123", "email@test.com", "M"),      // Too short name
                new RegistrationReq("janko", "", "email@test.com", "M"),        // Prázdne heslo
                new RegistrationReq("janko", "heslo123", "zly-email", "M"),      // Nevalidný email
                new RegistrationReq("janko", "heslo123", "email@test.com", "X"), // Nevalidné pohlavie
                new RegistrationReq("dsdsd", "", "", "")
        );
    }




}
