package sk.mvp.user_service.infra.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;
import sk.mvp.user_service.common.exception.auth.EmailNotVerifiedException;
import sk.mvp.user_service.user.entity.UserStatus;

@Component
public class QPostAuthenticationChecker  implements UserDetailsChecker {

    @Override
    public void check(UserDetails userDetails) {
        if (userDetails instanceof QUserDetail customUserDetails) {
            if (customUserDetails.getUserStatus() == UserStatus.PENDING_VERIFICATION) {
                throw new EmailNotVerifiedException("Email address is not verified.");
            }
        }

    }
}
