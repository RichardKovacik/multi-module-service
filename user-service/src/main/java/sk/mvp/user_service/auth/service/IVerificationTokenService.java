package sk.mvp.user_service.auth.service;

import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.auth.entity.VerificationToken;

public interface IVerificationTokenService {
    VerificationToken getValidVerificationToken(String token, VerificationTokenType verificationTokenType);
    VerificationToken createVerificationToken(User user, VerificationTokenType verificationTokenType);
    void deleteVerificationToken(String token);


}
