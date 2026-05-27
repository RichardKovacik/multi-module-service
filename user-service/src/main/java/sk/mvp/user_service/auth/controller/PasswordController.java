package sk.mvp.user_service.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.mvp.user_service.auth.dto.request.PasswordResetInitiateReq;
import sk.mvp.user_service.auth.dto.request.PasswordResetSubmitReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;
import sk.mvp.user_service.auth.service.IPasswordService;

@RestController
@RequestMapping("api/v1/auth/password")
public class PasswordController {
    private final IPasswordService passwordService;

    public PasswordController(IPasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping(value = "/reset-request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetInitiateReq request) {
        passwordService.initiatePasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/reset")
    public ResponseEntity<?> submitPasswordReset(@Valid @RequestBody PasswordResetSubmitReq request) {
        passwordService.verifyAndUpdatePasswordResetToken(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/reset/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") @NotNull @NotBlank String token) {
        passwordService.passwordResetTokenValidate(token);
        return ResponseEntity.ok().build();
    }
}
