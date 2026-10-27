package social.benji.benji_backend_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import social.benji.benji_backend_api.usermanagement.repository.LegacyUserRepository;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration class for Spring Boot with Spring Security 6+.
 * Configures authentication, CORS, password encoding, and authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final LegacyUserRepository legacyUserRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(LegacyUserRepository legacyUserRepository,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.legacyUserRepository = legacyUserRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configure UserDetailsService to load user by email.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> legacyUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Configure password encoder using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication provider with custom UserDetailsService.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configure authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    /**
     * Configure CORS settings.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://tellinbox.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure security filter chain with stateless session management.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                })
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (OTP request/verify/refresh/logout). The global
                // logout endpoint sits under /api/auth/** but is additionally
                // protected by the @AuthenticationPrincipal null-check in the controller.
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                // Lookup reads are public reference data; writes stay authenticated
                // (further restricted to ADMIN via @PreAuthorize on LookupController)
                .requestMatchers(HttpMethod.GET, "/api/lookups/**").permitAll()
                // Lookup writes (POST/PUT/PATCH/DELETE) are ADMIN-only; this mirrors the
                // @PreAuthorize on LookupController so 403 is decided at the request layer too.
                .requestMatchers(HttpMethod.POST, "/api/lookups/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/lookups/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/lookups/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/lookups/**").hasRole("ADMIN")
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // User endpoints - require authentication
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
                
                // Feedback endpoints
                .requestMatchers("/api/feedbacks/**").authenticated()
                
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .httpBasic(withDefaults())
            // Validate the Bearer access token (HS256 signature + expiry) and
            // populate the SecurityContext before authorization checks run.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
