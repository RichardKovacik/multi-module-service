package sk.mvp.user_service.user.factory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sk.mvp.user_service.auth.dto.RegistrationReq;
import sk.mvp.user_service.common.exception.RoleNotFoundException;
import sk.mvp.user_service.entity.Contact;
import sk.mvp.user_service.entity.Gender;
import sk.mvp.user_service.entity.Role;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.user.repository.RoleRepository;

import java.util.Set;

@Component
public class UserRegistrationFactory {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserRegistrationFactory(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }
    public User createUnverifiedUser(RegistrationReq registrationReq) {
        String hashPassword = passwordEncoder.encode(registrationReq.getPassword());
        Contact contact = new Contact(registrationReq.getEmail());
        Role role = roleRepository.findByName("USER").orElseThrow(() -> new RoleNotFoundException("Role USER not found"));

        User user = User.builder()
                .username(registrationReq.getUsername())
                .password(hashPassword)
                .gender(Gender.getValidGenderFromCode(registrationReq.getGenderCodeAsCharacter()))
                .roles(Set.of(role))
                .enabled(true)
                .emailVerified(false)
                .tokenVersion(1)
                .build();
        //set one to one relationship
        user.setContact(contact);
        return user;
    }
    public User createVerifiedAdmin(RegistrationReq registrationReq) {
        String hashPassword = passwordEncoder.encode(registrationReq.getPassword());
        Contact contact = new Contact(registrationReq.getEmail());
        Role role = roleRepository.findByName("ADMIN").orElseThrow(() -> new RoleNotFoundException("Role ADMIN not found"));

        User user = User.builder()
                .username(registrationReq.getUsername())
                .password(hashPassword)
                .gender(Gender.getValidGenderFromCode(registrationReq.getGenderCodeAsCharacter()))
                .roles(Set.of(role))
                .enabled(true)
                .emailVerified(true)
                .tokenVersion(1)
                .build();
        //set one to one relationship
        user.setContact(contact);
        return user;
    }
}
