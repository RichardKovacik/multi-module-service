package sk.mvp.user_service.infra.reddis;

public enum RedisCacheKey {
    // --- AUTH domain ---
    AUTH_ACCESS_BLACKLIST("auth:access:blacklist:%s"),
    AUTH_ACCESS_TOKEN_USER_VERSION("auth:access:user:version:%s"),
    AUTH_REFRESH_TOKEN_WHITELIST("auth:refresh:token:whitelist:%s"),
    AUTH_REFRESH_TOKEN_USER_SET("auth:refresh:user:%s"),
    AUTH_USER_BLACKLIST("auth:user:blacklist:%s"),

    // --- AUTH: BRUTE-FORCE PROTECTION
    //auth:login:attempts:<username>
    AUTH_LOGIN_ATTEMPTS("auth:login:attempts:%s"),
    //auth:login:lockout:<username>
    AUTH_LOGIN_LOCKOUT("auth:login:lockout:%s");

    private final String keyPrefix;

    RedisCacheKey(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyPrefix(Object... args) {
        return String.format(this.keyPrefix, args);
    }
}
