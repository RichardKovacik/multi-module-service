package sk.mvp.user_service.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sk.mvp.user_service.user.utils.GenderConverter;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {
    //TODO: create iniq indexes and Optimicstic locking

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", length = 20)
    private String firstName;

    // spravit v db constraint na min dlzku 3, + DTO validacia, nahradim literaly konstantami
    @Column(name = "last_name", length = 30)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(length = 30)
    private String password;

    // User nemoze existovat bez kontaktu, ked sa vymaze user vymaze sa aj kontakt
    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = false)
    private Contact contact;

    // constraint F alebo M
    @Column(nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender;

    //init value sets to 1, accesTokenVersion
    @Column(name ="token_version", nullable = false)
    private int tokenVersion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public User() {
    }

    public User(String username, String password, Contact contact, Gender gender) {
        this.username = username;
        this.password = password;
        this.contact = contact;
        this.gender = gender;
    }

    public UserStatus getStatus() {
        return status;
    }

    private void setStatus(UserStatus newStatus) {
        if (this.status.isNextStateValid(newStatus)) {
            this.status = newStatus;
        } else {
            throw new IllegalArgumentException("Cannot change status from " + this.status + " to " + newStatus);
        }
    }

    public void activate() {
        this.setStatus(UserStatus.ACTIVE);
    }
    public void block() {
        this.setStatus(UserStatus.BLOCKED);
    }
    public boolean isActivated() {
        return this.getStatus() == UserStatus.ACTIVE ||
                this.getStatus() == UserStatus.BLOCKED ||
                this.getStatus() == UserStatus.REMOVED;
    }

    public boolean isBlocked() {
        return this.getStatus() == UserStatus.BLOCKED;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Contact getContact() {
        return contact;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setContact(Contact contact) {
        this.contact = contact;

        if (contact != null) {
            contact.setUser(this);
        }
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String[] getRolesAsString() {
        return this.getRoles().stream()
                .map(Role::getName)
                .toArray(String[]::new);
    }
    //Spring security prefix
    public String[] getRolesAsStringWithPrefix() {
        return this.getRoles().stream()
                .map(role -> {
                    String name = role.getName();
                    return name.startsWith("ROLE_") ? name : "ROLE_" + name;
                })
                .toArray(String[]::new);
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

}
