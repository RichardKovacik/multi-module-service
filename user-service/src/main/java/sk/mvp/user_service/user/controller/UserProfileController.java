package sk.mvp.user_service.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.mvp.user_service.common.exception.InvalidTokenException;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.service.IUserService;
import sk.mvp.user_service.auth.jwt.JwtProvider;

@RestController
@RequestMapping(value = "api/v1/profile")
public class UserProfileController {
    private IUserService userService;
    private JwtProvider jwtProvider;

    public UserProfileController(IUserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping(value = "/me")
    public UserProfile getUserProfile(@CookieValue(name = "access_token", required = false) String cookieAccess,
                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = jwtProvider.extractAccessToken(authHeader, cookieAccess);

        if (token == null || token.isEmpty()) {
            throw new QApplicationException("Missing access token.", ErrorType.AUTH_BAD_REQUEST, null);
        }

        String username = jwtProvider.getUsernameFromAccessToken(token);
        return userService.getUserByUsername(username);
    }

    @PatchMapping(value = "/update")
    public ResponseEntity<?> updateUserProfileData(@CookieValue(name = "access_token", required = false) String cookieAccess,
                                                   @RequestBody(required = false) @Valid UserProfile userProfileDTO,
                                                   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = jwtProvider.extractAccessToken(authHeader, cookieAccess);

        if (token == null || token.isEmpty()) {
            throw new QApplicationException("Missing access token.", ErrorType.AUTH_BAD_REQUEST, null);
        }

        String username = jwtProvider.getUsernameFromAccessToken(token);
        userService.updateUserProfile(username, userProfileDTO);
        return ResponseEntity.ok().build();
    }

}
