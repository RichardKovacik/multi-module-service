package sk.mvp.user_service.admin.service;

import sk.mvp.user_service.admin.dto.request.UserStatusUpdateReq;
import sk.mvp.user_service.admin.dto.UserSummary;
import sk.mvp.user_service.admin.dto.response.AdminUserListItemResp;

import java.util.List;

public interface IAdminService {
    void assignRoleToUser(String username, String roleName);
    void unassignRoleFromUser(String username, String roleName);
    void deleteUserbyUsername(String userName);
    void deleteUserbyEmailOptimized(String email);
    List<AdminUserListItemResp> getUsers(int page, int rows);
    List<UserSummary> getUsersByGender(int page, int rows, String gender);
    void revokeTokens(String username);
    void setUserStatus(Long userId, UserStatusUpdateReq userStatusUpdateReq);
}
