package sk.mvp.user_service.admin.dto.response;

import sk.mvp.user_service.user.entity.User;

public record AdminUserListItemResp(
        Integer id,
        String email,
        String username,
        String lastName,
        String gender
){
    public static AdminUserListItemResp fromEntity(User user) {
        if (user == null) return null;
        String email = user.getContact() != null ? user.getContact().getEmail() : null;
        return new AdminUserListItemResp(
                user.getId(),
                email,
                user.getUsername(),
                user.getLastName(),
                user.getGender().getCodeAsString()
        );
    }
}
