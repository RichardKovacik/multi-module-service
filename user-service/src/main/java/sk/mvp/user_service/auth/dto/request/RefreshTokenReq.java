package sk.mvp.user_service.auth.dto.request;

public class RefreshTokenReq {
    private String token;

    public RefreshTokenReq(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
