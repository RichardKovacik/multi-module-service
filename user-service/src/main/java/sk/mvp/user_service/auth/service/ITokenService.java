package sk.mvp.user_service.auth.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import sk.mvp.user_service.infra.security.QUserDetail;
import sk.mvp.user_service.auth.dto.response.TokenPair;
import sk.mvp.user_service.common.exception.InvalidTokenException;

public interface ITokenService {
    /**
     * method to generate new pair of tokens from user details data
     * save refresh token to reddis
     * @return access and refresh token
     */
    TokenPair generateTokenPair(UserDetails userDetails);

    /**
     * Method to generate new acces token after acces token expires
     * Genreate also new refresh token and old one is deleted
     * @param refreshToken base64 encoded token
     * @return
     */
    TokenPair refreshTokens(String refreshToken);

    /**
     * validate acces token and get user deatils from it
     * @param accessToken
     * @return
     * @throws InvalidTokenException
     */
    QUserDetail getUserDetailFromAccessToken(String accessToken);

    Claims validateAccessToken(String accessToken);

    /**
     * add acces token to redis blacklist
     * @param accessToken
     */
    void revokeAccessToken(String accessToken);

    /**
     * delete refresh token from whitelist
     * @param refreshToken
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * Cached opetaion to get token version from db
     * 1st call hits db and then others hit cached token version stored in reddis
     * @param userName
     * @return token version as integer
     */
    int getTokenVersion(String userName);

    /**
     * method to revoke all tokens of user in case of security incident
     * only Admins are allowed to use it, it foces logout in any user device
     * @param userName
     */
    void revokeAllTokens(String userName);


}
