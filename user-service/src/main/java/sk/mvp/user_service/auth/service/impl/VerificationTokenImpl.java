package sk.mvp.user_service.auth.service.impl;

import org.springframework.stereotype.Service;
import sk.mvp.user_service.auth.entity.VerificationTokenType;
import sk.mvp.user_service.auth.factory.VerificationTokenFactory;
import sk.mvp.user_service.auth.service.IVerificationTokenService;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.auth.entity.VerificationToken;
import sk.mvp.user_service.user.repository.VerificationTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Service
public class VerificationTokenImpl implements IVerificationTokenService {
    private VerificationTokenRepository repository;
    private VerificationTokenFactory verificationTokenFactory;

    public VerificationTokenImpl(VerificationTokenRepository repository, VerificationTokenFactory verificationTokenFactory) {
        this.repository = repository;
        this.verificationTokenFactory = verificationTokenFactory;
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return this.repository.findByToken(token).orElseThrow(() -> new QApplicationException(ErrorType.VERIFICATION_TOKEN_INVALID));
    }

    @Override
    public VerificationToken createVerificationToken(User user, VerificationTokenType tokenType) {
        VerificationToken verificationToken = verificationTokenFactory.create(tokenType, user);
        // save verificationToken to DB
        return this.repository.save(verificationToken);
    }

    @Override
    public void deleteVerificationToken(String token) {

    }

    @Override
    public void invalidateVerificationToken(String token) {

    }
}
