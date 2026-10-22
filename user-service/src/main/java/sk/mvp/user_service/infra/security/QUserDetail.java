package sk.mvp.user_service.infra.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sk.mvp.user_service.user.entity.Role;
import sk.mvp.user_service.user.entity.User;
import sk.mvp.user_service.user.entity.UserStatus;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QUserDetail implements UserDetails {
    private long id;
    private String username;
    private String password;
    private List<SimpleGrantedAuthority> authorities;
    @Getter
    private UserStatus userStatus;

    public QUserDetail(String username, List<String> roles) {
        this.username = username;
        this.authorities = mapRolesStringToAuthorities(roles);
    }

    public QUserDetail(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = mapRolesToAuthorities(user.getRoles());
        this.userStatus = user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public long getId() {
        return id;
    }

    private List<SimpleGrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
    }

    private List<SimpleGrantedAuthority> mapRolesStringToAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .toList();
    }

    /**
     * ovveride custom equals function to compare two objects
     * Using only immutable attributes of object is very important
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QUserDetail that = (QUserDetail) o;
        return id == that.id && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    /**
     * Account is enabled ONLY if it is ACTIVE.
     * If DELETED or BLOCKED_BY_ADMIN, this returns false and throws DisabledException.
     */
    @Override
    public boolean isEnabled() {
        return this.userStatus == UserStatus.ACTIVE || this.userStatus == UserStatus.PENDING_VERIFICATION;
    }

}
