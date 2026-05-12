package sk.mvp.user_service.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record UserStatusUpdateReq(
        @NotNull
        UserStatus status,
        String reason) implements Serializable {
}
