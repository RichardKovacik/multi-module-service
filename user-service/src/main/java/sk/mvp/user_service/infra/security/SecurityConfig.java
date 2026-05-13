package sk.mvp.user_service.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sk.mvp.user_service.auth.jwt.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private JwtAuthFilter jwtAuthFilter;
    private QPostAuthenticationChecker qPostAuthenticationChecker;
    private QUserDetailsService qUserDetailsService;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          QPostAuthenticationChecker qPostAuthenticationChecker,
                          QUserDetailsService qUserDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.qPostAuthenticationChecker = qPostAuthenticationChecker;
        this.qUserDetailsService = qUserDetailsService;
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
                                "/api/v1/auth/refresh/tokens",
                                "/api/v1/auth/email/confirm",
                                "/swagger-ui/**","/v3/api-docs/**",
                                "/actuator/**").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
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


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // set provierd encoder adn userdetail service
        provider.setUserDetailsService(qUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        // custom checker
        provider.setPostAuthenticationChecks(qPostAuthenticationChecker);

        return provider;
    }


}
