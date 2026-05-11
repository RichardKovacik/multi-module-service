package sk.mvp.user_service.auth.jwt;

import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token");

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }

}
