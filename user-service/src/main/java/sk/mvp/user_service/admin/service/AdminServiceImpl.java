package sk.mvp.user_service.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mvp.user_service.admin.dto.response.AdminUserListItemResp;
import sk.mvp.user_service.user.entity.UserStatus;
import sk.mvp.user_service.admin.dto.request.UserStatusUpdateReq;
import sk.mvp.user_service.admin.dto.UserSummary;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.user.entity.Gender;
import sk.mvp.user_service.user.entity.Role;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.projections.UserSummaryProjection;
import sk.mvp.user_service.user.repository.RoleRepository;
import sk.mvp.user_service.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements IAdminService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private ITokenService jwtService;

    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            ITokenService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void assignRoleToUser(String username, String roleName) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (roleName == null || roleName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new QApplicationException("User with username " + username + " not found", ErrorType.USER_NOT_FOUND, null));

        Role role = roleRepository.findByName(roleName).orElseThrow(
                () -> new QApplicationException("Role with name " + roleName + " not found", ErrorType.ROLE_NOT_FOUND, null));

        if (user.getRoles().contains(role)) {
            throw new QApplicationException(String.format("User %s already has %s role", username, roleName), ErrorType.ROLE_ALREADY_ASSIGNED, null);
        }

        user.getRoles().add(role);

        userRepository.save(user);
    }

    @Override
    public void unassignRoleFromUser(String username, String roleName) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (roleName == null || roleName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new QApplicationException("User with username " + username + " not found", ErrorType.USER_NOT_FOUND, null));

        Role role = roleRepository.findByName(roleName).orElseThrow(
                () -> new QApplicationException("Role with name " + roleName + "not found", ErrorType.ROLE_NOT_FOUND, null));

        if (!user.getRoles().contains(role)) {
            throw new RuntimeException("User does not have this role");
        }
        user.getRoles().remove(role);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserbyUsername(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        User user = userRepository.findByUsername(userName).orElseThrow(() ->
                new QApplicationException("User with username " + userName + " not found", ErrorType.USER_NOT_FOUND, null));
        userRepository.delete(user);
        //TODO: log user has been deleted
    }

    @Override
    @Transactional
    public void deleteUserbyEmailOptimized(String email) {
        if(email == null || email.isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }

        userRepository.deleteUserByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserListItemResp> getUsers(int pageNumber, int pageSize) {
        Page<User> users = userRepository.findAll(PageRequest.of(pageNumber, pageSize));
        if (users.isEmpty()) {
            return List.of();
        }
        return users.getContent()
                .stream()
                .map(AdminUserListItemResp::fromEntity)
                .toList();
    }

    @Override
    public List<UserSummary> getUsersByGender(int page, int rows, String gender) {
        if (gender == null || gender.isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }

        char genderCode = Character.toUpperCase(gender.charAt(0));

        Page<UserSummaryProjection> users = userRepository.findAllByGender(Gender.getValidGenderFromCode(genderCode), PageRequest.of(page, rows));
        if (users.isEmpty()){
            return List.of();
        }
        return users.getContent()
                .stream()
                .map(UserSummary::new)
                .collect(Collectors.toList());
    }

    @Override
    public void revokeTokens(String username) {
        jwtService.revokeAllTokens(username);
    }

    @Override
    @Transactional
    public void setUserStatus(Long userId, UserStatusUpdateReq req) {
        boolean isEnabled = req.status() == UserStatus.ENABLED;
        userRepository.setEnabled(userId, isEnabled);
        // remove actual user sessions only if ist BLOCKED
        if (!isEnabled) {
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new QApplicationException("User with id " + userId + " not found", ErrorType.USER_NOT_FOUND, null));
            this.revokeTokens(user.getUsername());
        }

    }
}
