package sk.mvp.user_service.auth.entity;

import jakarta.persistence.*;
import sk.mvp.user_service.entity.User;

import java.time.Instant;


@Entity
@Table(
        name = "verification_token",
        indexes = {
                @Index(name = "idx_token_value", columnList = "token", unique = true),
                @Index(name = "idx_token_user", columnList = "user_id")
        }
)
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationTokenType verificationTokenType;

    @Column(nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean used = false;

    // =========================
    // Relations
    // =========================

//    @PrePersist
//    public void prePersist() {
//        this.createdAt = Instant.now();
//    }

    // =========================
    // Constructors
    // =========================

    public VerificationToken() {}

    public VerificationToken(String token, Instant expiresAt, User user, VerificationTokenType verificationTokenType) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
        this.verificationTokenType = verificationTokenType;
    }
// =========================
    // Getters & Setters
    // =========================


    public void setId(Long id) {
        this.id = id;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VerificationTokenType getVerificationTokenType() {
        return verificationTokenType;
    }

    public void setVerificationTokenType(VerificationTokenType verificationTokenType) {
        this.verificationTokenType = verificationTokenType;
    }
}
