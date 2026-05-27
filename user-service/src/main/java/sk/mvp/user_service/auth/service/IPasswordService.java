package sk.mvp.user_service.auth.service;

import sk.mvp.user_service.auth.dto.request.PasswordResetInitiateReq;
import sk.mvp.user_service.auth.dto.request.PasswordResetSubmitReq;
import sk.mvp.user_service.auth.dto.response.VerificationTokenResponse;

public interface IPasswordService {
    void initiatePasswordReset(PasswordResetInitiateReq request);
    void passwordResetTokenValidate(String token);
    void verifyAndUpdatePasswordResetToken(PasswordResetSubmitReq request);
}
