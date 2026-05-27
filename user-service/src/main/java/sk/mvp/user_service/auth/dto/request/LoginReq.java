package sk.mvp.user_service.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank String username,
        @NotBlank String password) {
}
