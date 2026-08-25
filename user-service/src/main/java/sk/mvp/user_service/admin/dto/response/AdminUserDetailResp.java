package sk.mvp.user_service.admin.dto.response;

import sk.mvp.user_service.user.entity.UserStatus;

public record AdminUserDetailResp(
        Integer id,
        String username,
        String lastName,
        String gender,
        String email,
        String phoneNumber,
        boolean isEmailVerified,
        UserStatus status
) {
}
