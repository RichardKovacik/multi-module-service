package sk.mvp.user_service.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import sk.mvp.user_service.user.entity.UserStatus;

import java.io.Serializable;

public record UserStatusUpdateReq(
        @NotNull
        UserStatus status,
        String reason) implements Serializable {
}
