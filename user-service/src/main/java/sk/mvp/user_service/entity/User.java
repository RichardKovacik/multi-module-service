package sk.mvp.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
            optional = false,
            fetch = FetchType.LAZY)
    private Contact contact;

    // constraint F alebo M
    @Column(nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender;

    // true if email has been verfied
    private boolean emailVerified;

    //init value sets to 1, accesTokenVersion
    @Column(name ="token_version", nullable = false)
    private int tokenVersion;

    // false if user was disabled by admin
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
    }

    public User(String username, String password, Contact contact, Gender gender) {
        this.username = username;
        this.password = password;
        this.contact = contact;
        this.gender = gender;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
