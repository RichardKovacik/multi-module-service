package sk.mvp.user_service.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.mvp.user_service.common.exception.data.QErrorResponse;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.service.IUserService;
import sk.mvp.user_service.auth.jwt.JwtProvider;

@RestController
@RequestMapping(value = "api/v1/profile")
@Tag(name = "User profile", description = "Endpoints for retrieving and updating the authenticated user's profile")
public class UserProfileController {
    private IUserService userService;
    private JwtProvider jwtProvider;

    public UserProfileController(IUserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @Operation(
            summary = "Get authenticated user profile",
            description = "Returns the profile of the user identified by the access token from the Authorization header or access_token cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfile.class))),
            @ApiResponse(responseCode = "400", description = "Access token is missing",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Access token is invalid or expired",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class)))
    })
    @GetMapping(value = "/me")
    public UserProfile getUserProfile(
            @Parameter(description = "Access token stored in an HTTP cookie")
            @CookieValue(name = "access_token", required = true) String cookieAccess) {
        String username = jwtProvider.getUsernameFromAccessToken(cookieAccess);
        return userService.getUserByUsername(username);
    }

    @Operation(
            summary = "Update authenticated user profile",
            description = "Updates editable fields of the authenticated user's profile. The profile is identified from the access token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Access token is missing or request body is malformed",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Access token is invalid or expired",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "User profile data failed validation",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = QErrorResponse.class)))
    })
    @PatchMapping(value = "/update")
    public ResponseEntity<?> updateUserProfileData(
            @Parameter(description = "Access token stored in an HTTP cookie")
            @CookieValue(name = "access_token", required = true) String cookieAccess,
            @Parameter(description = "Fields to update in the user profile")
            @RequestBody(required = false) @Valid UserProfile userProfileDTO) {

        String username = jwtProvider.getUsernameFromAccessToken(cookieAccess);
        userService.updateUserProfile(username, userProfileDTO);
        return ResponseEntity.ok().build();
    }

}
