package sk.mvp.user_service.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.mvp.user_service.admin.dto.request.UserStatusUpdateReq;
import sk.mvp.user_service.admin.dto.UserSummary;
import sk.mvp.user_service.admin.dto.response.AdminUserListItemResp;

import java.util.List;

public interface IAdminService {
    void assignRoleToUser(String username, String roleName);
    void unassignRoleFromUser(String username, String roleName);
    void deleteUserbyUsername(String userName);
    void deleteUserbyEmailOptimized(String email);
    Page<AdminUserListItemResp> getUsers(Pageable pageable);
    List<UserSummary> getUsersByGender(int page, int rows, String gender);
    void revokeTokens(String username);
    void setUserStatus(Long userId, UserStatusUpdateReq userStatusUpdateReq);
}
