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

import com.pond.server.service.JwtService;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        
        //Extracts the JWT token from the request header
        final String authHeader = request.getHeader("Authorization");

        //If no header or the header doesn't start with bearer, then no token so proceed downstream
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /* Parses the JWT token from the header and extracts the user email
         1. Extract the token
         2. Extract the email
         3. Read the security context to see if the user is already authenticated
         4. If the user is not authenticated, load the user details from the database
         5. If the token is valid then create an authentication token and set it in the security context
         6. Advance to the next request whether authenticated or not
         */
        try{
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null){
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
            filterChain.doFilter(request, response);
        } catch(Exception exception){
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}
