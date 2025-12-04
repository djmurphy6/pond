package com.pond.server.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    // Validates credentials and builds the authentication object
    private final AuthenticationProvider authenticationProvider;
    // Extracts a JWT from the request and validates it, then sets the security
    // context
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor
    public SecurityConfiguration(AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /*
     * Defines the security filter chain:
     * 1. Disables CSRF protection
     * 2. Makes sure all requests under /auth are permitted, while any other request
     * is authenticated
     * 3. Sets the session creation policy to STATELESS, meaning no session is
     * created or used, request must carry its own credentials(JWT)
     * 4. Registers the authentication provider so the chain knows how to
     * authenticate the request
     * 5. Inserts the JWT filter early in the chain so the token validation runs on
     * every request
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll() //Allow websockets
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/ws-test/*").permitAll() // Make sure websockets are working and its just security blocking
                        .requestMatchers("/chat/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(
                    HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                })
            );

        return http.build();
    }

    // Configures CORS to allow requests from the frontend
    // Set FRONTEND_URL environment variable for production (set in Render)
    // Set ADDITIONAL_ORIGINS for comma-separated list of extra origins to allow
    @Value("${FRONTEND_URL:}")
    private String frontendUrl;
    
    @Value("${ADDITIONAL_ORIGINS:}")
    private String additionalOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Build allowed origins list
        List<String> allowedOrigins = new ArrayList<>();
        
        // Add production frontend URL if provided
        if (frontendUrl != null && !frontendUrl.isEmpty() && !frontendUrl.equals("http://localhost:3000")) {
            allowedOrigins.add(frontendUrl);
        }
        
        // Add additional origins from environment variable (comma-separated)
        if (additionalOrigins != null && !additionalOrigins.isEmpty()) {
            String[] origins = additionalOrigins.split(",");
            for (String origin : origins) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    allowedOrigins.add(trimmed);
                }
            }
        }
        
        // Always allow localhost for development/testing
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://localhost:8080");
        
        //Production/test URLs
        allowedOrigins.add("https://pond-kohl.vercel.app");
        
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Set-Cookie", "X-Access-Token", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registers the CORS configuration for all paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
