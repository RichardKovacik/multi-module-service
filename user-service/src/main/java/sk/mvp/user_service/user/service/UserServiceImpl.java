package sk.mvp.user_service.user.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.user.dto.ContactResp;
import sk.mvp.user_service.user.dto.UserProfile;
import sk.mvp.user_service.user.repository.RoleRepository;
import sk.mvp.user_service.user.repository.UserRepository;

import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserProfile getUserByFirstName(String firstName) {
        User user = userRepository.findByFirstName(firstName).orElseThrow(() -> new QApplicationException(
                String.format("User with firstName %s not found",
                firstName),
                ErrorType.USER_NOT_FOUND, null));
        return new UserProfile(user);
    }

    @Override
    @Transactional
    public UserProfile getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new QApplicationException("User with email " + email + " not found", ErrorType.USER_NOT_FOUND, null));
        return new UserProfile(user);
    }

    @Override
    @Transactional
    public UserProfile getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new QApplicationException("User with username " + username + " not found", ErrorType.USER_NOT_FOUND, null));
        return new UserProfile(user);
    }

    @Transactional
    @Override
    public void updateUserProfile(String userName, UserProfile userProfileDTO) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        User user = userRepository.findByUsername(userName).orElseThrow(() ->
                new QApplicationException("User with username " + userName + " not found", ErrorType.USER_NOT_FOUND, null));

        if (userProfileDTO.getFirstName() != null && !userProfileDTO.getFirstName().isEmpty()) {
            user.setFirstName(userProfileDTO.getFirstName());
        }
        if (userProfileDTO.getLastName() != null && !userProfileDTO.getLastName().isEmpty()) {
            user.setLastName(userProfileDTO.getLastName());
        }
        ContactResp contactDto = userProfileDTO.getContact();
        if (contactDto != null) {
            // pokial ide zmenit email a novy sa rovna staremu tak nespravi mziadnu zmenu v db preskocim
            if (contactDto.email() != null && !contactDto.email().isEmpty()
                    && !contactDto.email().equals(user.getContact().getEmail())){
                //check if email already exists
                Optional<User> foundedUser = userRepository.findByEmail(contactDto.email() );
                if (foundedUser.isPresent()) {
                    throw new QApplicationException(String.format("Email is already in use", contactDto.email()), ErrorType.EMAIL_DUPLICATED, null);
                }
                user.getContact().setEmail(contactDto.email());
            }
            if (contactDto.phoneNumber() != null && !contactDto.phoneNumber().isEmpty()) {
                user.getContact().setPhoneNumber(contactDto.phoneNumber());
            }
        }

        userRepository.save(user);

    }

}
