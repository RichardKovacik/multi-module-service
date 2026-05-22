package sk.mvp.user_service.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VerificationLinkService {

    @Value("${app.backend.protocol}")
    private String protocol;

    @Value("${app.backend.domain}")
    private String domain;

    @Value("${app.backend.port}")
    private String port;

    @Value("${app.backend.email-confirm-uri}")
    private String emailConfirmUri;

    public String generateConfirmationEmailUrl(String generatedToken) {
        return UriComponentsBuilder.newInstance()
                .scheme(protocol)      // http
                .host(domain)          // localhost
                .port(port)            // 8081
                .path(emailConfirmUri) // /api/v1/auth/email/confirm
                .queryParam("token", generatedToken) // ?token=XYZ
                .build()
                .toUriString();
    }
}

