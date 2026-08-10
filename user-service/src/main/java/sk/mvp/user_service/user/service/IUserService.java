package sk.mvp.user_service.user.service;

import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.user.dto.UserProfile;

public interface IUserService {
    UserProfile getUserByFirstName(String firstName);
    UserProfile getUserProfileByEmail(String email);
    User getUserByEmail(String email);
    UserProfile getUserByUsername(String username);
    void updateUserProfile(String userName, UserProfile userProfileDTO);
}
