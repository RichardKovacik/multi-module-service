package sk.mvp.user_service.common.constants;

public class AuthConts {
    public static byte MAX_LOGIN_ATTEMPTS = 3;
//    public static String REDISS_AUTH_LOGIN_ATTEMPTS_USER_COLL = "auth:login_attempts:user:";
//    public static String REDISS_AUTH_BLACKLIST_USER_COLL = "auth:blacklist:user:";
    public static int USER_LOCKED_TTL_IN_SECONDS = 60; // toto aj na 1h zablokujem
    public static int USER_LOGIN_ATTEMPTS_REDIS_TTL_IN_SECONDS = 300;// prod casy podstatne dlshie toto aj na 5h
//    public static String WEB_LOGIN_API_PATH = "/api/auth/web/login";
}
