package sk.mvp.user_service.auth.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sk.mvp.user_service.auth.service.ITokenService;
import sk.mvp.user_service.common.exception.data.ErrorType;
import sk.mvp.user_service.infra.security.QUserDetail;
import sk.mvp.user_service.auth.dto.response.TokenPair;
import sk.mvp.user_service.common.exception.QApplicationException;
import sk.mvp.user_service.entity.User;
import sk.mvp.user_service.infra.reddis.RedisCacheKey;
import sk.mvp.user_service.user.repository.UserRepository;
import sk.mvp.user_service.infra.reddis.IRedisService;
import sk.mvp.user_service.auth.jwt.JwtProvider;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements ITokenService {
    private IRedisService redisService;
    private UserRepository userRepository;
    private JwtProvider jwtProvider;

    public TokenServiceImpl(IRedisService redisService,
                            UserRepository userRepository,
                            JwtProvider jwtProvider) {
        this.redisService = redisService;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public TokenPair generateTokenPair(UserDetails userDetails) {
        int tokenVersion = getTokenVersion(userDetails.getUsername());
        String jtiRefresh = UUID.randomUUID().toString();
        String jtiAccess = UUID.randomUUID().toString();

        String accessToken = jwtProvider.generateAccessToken(userDetails.getUsername(),
                tokenVersion,
                jtiAccess,
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new));
        String refreshToken = jwtProvider.generateRefreshToken(jtiRefresh);
        String userRefreshTokensKey = RedisCacheKey.AUTH_REFRESH_TOKEN_USER_SET.getKeyPrefix(userDetails.getUsername());

        //add refresh token to whitelist
        redisService.set(RedisCacheKey.AUTH_REFRESH_TOKEN_WHITELIST.getKeyPrefix(jtiRefresh), userDetails.getUsername(),
                jwtProvider.getRefreshTokenExpiration());

        // add refresh token to reddis userName: setTokens(jti,jti,jti)
        redisService.addValueToSet(userRefreshTokensKey, jtiRefresh);


        return new TokenPair(refreshToken, accessToken);
    }

    @Override
    public TokenPair refreshTokens(String refreshToken) {
        //check if refresh token is valid, signing, expiratuon, type, fields...
        jwtProvider.validateRefreshToken(refreshToken);
        //parse claims from token
        Claims claims = jwtProvider.parseClaimsFromRefreshToken(refreshToken);
        String refreshKey =  RedisCacheKey.AUTH_REFRESH_TOKEN_WHITELIST.getKeyPrefix(claims.getId());

        //check if refresh is in whitelist
        String userName = String.valueOf(redisService.get(refreshKey)
                .orElseThrow(() -> new QApplicationException("Refresh token reuse detected", ErrorType.TOKEN_REUSED_DETECTED, null)));
        //delete actual refresh token from whitelist
        revokeRefreshToken(refreshToken);
        //get userdetail from db
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new QApplicationException("User with username " + userName + " not found", ErrorType.USER_NOT_FOUND, null));

        //generate new pairs and also add in reddis whotelist noew refresh token
        return generateTokenPair(new QUserDetail(user));
    }

    @Override
    public QUserDetail getUserDetailFromAccessToken(String accessToken) {
       return jwtProvider.getUserDetailFromAccessToken(accessToken);
    }

    @Override
    public Claims validateAccessToken(String accessToken) {
        Claims claims = jwtProvider.parseAccessToken(accessToken);
        String username = claims.getSubject();

        int tokenVersion = getTokenVersion(username);
        jwtProvider.validateAccessToken(claims, tokenVersion);

        // validate if access token is in blacklist
        String key = RedisCacheKey.AUTH_ACCESS_BLACKLIST.getKeyPrefix(claims.getId());
        if (redisService.has(key)) {
            throw new QApplicationException("Access token is in blacklist. Possible security breach !!", ErrorType.TOKEN_REUSED_DETECTED, null);
        }
        return claims;

    }

    @Override
    public void revokeAccessToken(String accessToken) {
        //add access token to blacklist for reaming time to live of token, used when logout
        Claims claims = jwtProvider.parseAccessToken(accessToken);
        String key = RedisCacheKey.AUTH_ACCESS_BLACKLIST.getKeyPrefix(claims.getId());
        redisService.set(key, claims.getId(), jwtProvider.ttlUntilExpiration(claims));
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        String tokenId = jwtProvider.parseClaimsFromRefreshToken(refreshToken).getId();
        String key = RedisCacheKey.AUTH_REFRESH_TOKEN_WHITELIST.getKeyPrefix(tokenId);
        redisService.delete(key);
    }


    @Override
    public int getTokenVersion(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }

        String key = RedisCacheKey.AUTH_ACCESS_TOKEN_USER_VERSION.getKeyPrefix(userName);

        // try hit redis chache
        Optional<String> cachedValue = redisService.get(key).map(Object::toString);
        if (cachedValue.isPresent()) {
            return Integer.parseInt(cachedValue.get());
        }
        // cache is empty, try hit real db
        int tokenVersion = userRepository.getTokenVersion(userName)
                .orElseThrow(() -> new QApplicationException("User with username " + userName + " not found", ErrorType.USER_NOT_FOUND, null));

        // save value to the redis cache
        redisService.set(key,
                String.valueOf(tokenVersion),
                jwtProvider.getAccessTokenExpiration());

        return tokenVersion;
    }

    @Override
    @Transactional
    public void revokeAllTokens(String userName) {

        // increase token version for security concern
        userRepository.incrementTokenVersion(userName);

        //get all user refresh tokens from set
        String userRefreshTokensKey = RedisCacheKey.AUTH_REFRESH_TOKEN_USER_SET.getKeyPrefix(userName);
        Set<String> refreshJtis = redisService.getSet(userRefreshTokensKey).stream().map(Object::toString).collect(Collectors.toSet());

        //delete refresh token from whitelist
        for(String key : refreshJtis) {
            redisService.delete(RedisCacheKey.AUTH_REFRESH_TOKEN_WHITELIST.getKeyPrefix(key));
        }

        //delete all refresh tokens from set
        redisService.delete(userRefreshTokensKey);

        // delete cached reddis  auth:user:tokenVersion
        String tokenVersionKey = RedisCacheKey.AUTH_ACCESS_TOKEN_USER_VERSION.getKeyPrefix(userName);
        redisService.delete(tokenVersionKey);
    }
}
