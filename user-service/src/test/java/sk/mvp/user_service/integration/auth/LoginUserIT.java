package sk.mvp.user_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import sk.mvp.user_service.auth.dto.request.LoginReq;
import sk.mvp.user_service.auth.jwt.JwtConfig;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.infra.reddis.IRedisService;
import sk.mvp.user_service.auth.jwt.JwtProvider;
import sk.mvp.user_service.integration.BaseIntegrationTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@Sql(scripts = "/data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class LoginUserIT extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private JwtConfig jwtConfig;


    private static Stream<Arguments> provideHappyPathLoginCredentials() {
        return Stream.of(
                Arguments.of("cdavis", "password123"),
                Arguments.of("bbrown", "password123"),
                Arguments.of("ewilson", "password777")
        );
    }
    private static Stream<Arguments> provideBadCredetials() {
        return Stream.of(
                Arguments.of("cdaviss", "password123"),
                Arguments.of("bbrown", "password1233"),
                Arguments.of("ewilson22", "password777")
        );
    }

    @ParameterizedTest
    @Transactional
    @MethodSource("provideHappyPathLoginCredentials")
    void shouldSuccessfullyLogin(String username, String password) throws Exception {
        LoginReq loginReq = new LoginReq(username, password);
        String jsonBody = objectMapper.writeValueAsString(loginReq);

        // act and assert
        MvcResult result = mockMvc.perform(post("/api/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        // check if refresh token is stored in reddis in whitelist
        Cookie refreshTokenCookie = result.getResponse().getCookie("refresh_token");
        //assert
        assert refreshTokenCookie != null;
        Claims claims = JwtProvider.parseClaimsFromJwtToken(refreshTokenCookie.getValue(), jwtConfig.getRefreshKey());
        boolean isRefreshTokenAddToWhiteList = redisService.has("auth:refresh:token:"+ claims.getId());

        assertTrue(isRefreshTokenAddToWhiteList);

    }

    @ParameterizedTest
    @MethodSource("provideBadCredetials")
    @Transactional
    void shouldFailLoginWhenInvalidCredentials(String username, String password) throws Exception {
        LoginReq loginReq = new LoginReq(username, password);
        String jsonBody = objectMapper.writeValueAsString(loginReq);

        mockMvc.perform(post("/api/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // assert - HTTP vrstva
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.errorType").value(ErrorType.AUTH_INVALID_CREDENTIALS.toString()))
                .andExpect(jsonPath("$.error.statusCode").value(ErrorType.AUTH_INVALID_CREDENTIALS.getStatus()))
                .andExpect(jsonPath("$.error.path").value("/api/auth/web/login"))
                .andExpect(jsonPath("$.error.message").exists());
    }
}
