package sk.mvp.user_service.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountRestoreReq(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
