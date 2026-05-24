package sk.mvp.user_service.auth.service;

import sk.mvp.user_service.auth.dto.RegistrationReq;
import sk.mvp.user_service.auth.dto.TokenPair;
import sk.mvp.user_service.auth.dto.LoginReq;
import sk.mvp.user_service.auth.dto.VerificationTokenResponse;
import sk.mvp.user_service.user.dto.UserProfile;

public interface IAuthService {
    TokenPair loginUser(LoginReq loginReq);
    TokenPair refreshTokens(String refreshToken);
    UserProfile registerUser(RegistrationReq user);
    void logout(String refreshToken, String accessToken);
    VerificationTokenResponse verifyAndUpdateEmailVerificationToken(String verificationToken);
    // void logout(HttpServletRequest request);
}
