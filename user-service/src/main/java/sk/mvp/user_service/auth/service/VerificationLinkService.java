package sk.mvp.user_service.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mvp.user_service.auth.config.BackendUrlConfig;
import sk.mvp.user_service.auth.entity.VerificationTokenType;

@Service
@RequiredArgsConstructor
public class VerificationLinkService {
    private final BackendUrlConfig urlConfig;

    public String generateVerificationUrl(VerificationTokenType tokenType, String token) {
        String specificPath = urlConfig.getUriFor(tokenType);

        return UriComponentsBuilder.newInstance()
                .scheme(urlConfig.getProtocol())
                .host(urlConfig.getDomain())
                .port(urlConfig.getPort())
                .path(specificPath)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
