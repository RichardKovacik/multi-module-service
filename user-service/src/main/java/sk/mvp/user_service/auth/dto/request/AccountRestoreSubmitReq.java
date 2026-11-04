package sk.mvp.user_service.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AccountRestoreSubmitReq(
        @NotBlank(message = "Token is required") String token) {
}
