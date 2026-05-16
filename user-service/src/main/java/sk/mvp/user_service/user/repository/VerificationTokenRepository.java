package sk.mvp.user_service.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mvp.user_service.auth.entity.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    void deleteByToken(String token);
    Optional<VerificationToken> findByToken(String token);
}
