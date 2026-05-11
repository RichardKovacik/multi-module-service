package sk.mvp.user_service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutReq(@NotBlank String refreshToken) {
}
