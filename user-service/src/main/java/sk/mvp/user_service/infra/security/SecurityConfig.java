package sk.mvp.user_service.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sk.mvp.user_service.infra.filter.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private JwtAuthFilter jwtAuthFilter;
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                // csfr simple config
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .cors(AbstractHttpConfigurer::disable) // Disable CORS (or configure if needed)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/email/confirm",
                                "/actuator/**").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/profile/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        // Add the JWT Token filter before the UsernamePasswordAuthenticationFilter
                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                http.addFilterAt(qUsernamePasswordAuthFilter, UsernamePasswordAuthenticationFilter.class);
        if (sslEnabled) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }
        return http.build();
    }
//    @Bean
//    public QUsernamePasswordAuthFilter qUsernamePasswordAuthFilter(
//            AuthenticationManager authenticationManager,
//            AuthenticationFailureHandler failureHandler,
//            AuthenticationSuccessHandler successHandler,
//            ObjectMapper objectMapper,
//            IRedisService redisService) {
//        return new QUsernamePasswordAuthFilter(authenticationManager, successHandler, failureHandler, objectMapper, redisService
//        );
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
