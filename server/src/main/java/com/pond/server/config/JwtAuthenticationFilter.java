package com.pond.server.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.WebUtils;

import com.pond.server.service.JwtService;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Runs on every request, validates the JWT token and sets the authenticated user in the security context
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    //Resolves any exceptions that occur during the request
    private final HandlerExceptionResolver handlerExceptionResolver;
    //Validates the JWT token
    private final JwtService jwtService;
    //Loads the user details from the database
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService,HandlerExceptionResolver handlerExceptionResolver){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {

        String jwt = null;
    
        // 1) Try Authorization header (optional)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }
    
        // 2) Fallback to cookie "accessToken"
        if (jwt == null) {
            Cookie cookie = WebUtils.getCookie(request, "accessToken");
            if (cookie != null) {
                jwt = cookie.getValue();
            }
        }
    
        if (jwt == null || jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
    
        try {
            final String userEmail = jwtService.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
    
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }

    }
}
