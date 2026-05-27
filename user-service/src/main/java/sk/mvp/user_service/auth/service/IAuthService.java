package sk.mvp.user_service.auth.service;

import sk.mvp.user_service.auth.dto.request.RegistrationReq;
import sk.mvp.user_service.auth.dto.response.TokenPair;
import sk.mvp.user_service.auth.dto.request.LoginReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;
import sk.mvp.user_service.user.dto.UserProfile;

public interface IAuthService {
    TokenPair loginUser(LoginReq loginReq);
    TokenPair refreshTokens(String refreshToken);
    UserProfile registerUser(RegistrationReq user);
    void logout(String refreshToken, String accessToken);
    VerificationTokenResponse verifyAndUpdateEmailVerificationToken(String verificationToken);
  //  VerificationTokenResponse verifyAndUpdatePasswordResetVerificationToken(String token, String newPassword);
    // void logout(HttpServletRequest request);
}
