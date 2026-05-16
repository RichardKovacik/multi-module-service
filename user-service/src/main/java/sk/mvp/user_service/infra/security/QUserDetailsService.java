package sk.mvp.user_service.infra.security;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.mvp.user_service.common.exception.AccountLockedExp;
import sk.mvp.user_service.user.repository.UserRepository;

@Service
public class QUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public QUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public QUserDetail loadUserByUsername(String username) throws UsernameNotFoundException, AccountLockedExp {
        sk.mvp.user_service.entity.User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username not found"));
        return new QUserDetail(user);
    }
}
