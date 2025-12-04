package com.pond.server.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import com.pond.server.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service class for JWT (JSON Web Token) operations.
 * Handles token generation, validation, and claim extraction for authentication.
 * Supports both access tokens and refresh tokens with different expiration times.
 */
@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.access-expiration-time}")
    private long accessExpiration;

    /**
     * Extracts the username (email) from a JWT token.
     *
     * @param token the JWT token string
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using a resolver function.
     *
     * @param <T> the type of the claim to extract
     * @param token the JWT token string
     * @param claimsResolver function that extracts the desired claim from Claims object
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        //Claims object is the payload of the token, final means it cannot be changed once set
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token with default expiration and no extra claims.
     *
     * @param userDetails the user details to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with extra claims and default expiration.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generates a short-lived access token.
     *
     * @param userDetails the user details to include in the token
     * @return the generated access token string
     */
    public String generateAccessToken(UserDetails userDetails){
        return buildToken(new HashMap<>(), userDetails, accessExpiration);
    }

    /**
     * Gets the default JWT expiration time in milliseconds.
     *
     * @return the expiration time in milliseconds
     */
    public long getExpirationTime(){
        return jwtExpiration;
    }

    /**
     * Gets the access token expiration time in milliseconds.
     *
     * @return the access token expiration time in milliseconds
     */
    public long getAccessExpirationTime(){
        return accessExpiration;
    }

    /**
     * Builds a JWT token with specified claims, user details, and expiration.
     * Uses the user's email as the subject claim.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details to include in the token
     * @param expiration the token expiration time in milliseconds
     * @return the built JWT token string
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration){
        // Prefer email as subject to match UserDetailsService lookup; fallback to username
        String subject;
        try {
            subject = (String) userDetails.getClass().getMethod("getEmail").invoke(userDetails);
        } catch (Exception ignored) {
            subject = userDetails.getUsername();
        }

        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Validates a JWT token against user details.
     * Checks that the token's subject matches the user's email and is not expired.
     *
     * @param token the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String subject = extractUsername(token); // subject is the email
        String principalIdentifier;
        if (userDetails instanceof User u){
           principalIdentifier = u.getEmail();
        } else {
            try {
                principalIdentifier = (String) userDetails.getClass().getMethod("getEmail").invoke(userDetails);
            } catch (Exception e) {
                principalIdentifier = userDetails.getUsername();
            }
        }
        return (subject.equals(principalIdentifier) && !isTokenExpired(token));
    }

    /**
     * Validates a refresh token.
     * Delegates to isTokenValid for validation.
     *
     * @param token the refresh token to validate
     * @param userDetails the user details to validate against
     * @return true if refresh token is valid, false otherwise
     */
    public boolean isRefreshTokenValid(String token, UserDetails userDetails){
        return isTokenValid(token, userDetails);
    }

    /**
     * Validates an access token.
     * Delegates to isTokenValid for validation.
     *
     * @param token the access token to validate
     * @param userDetails the user details to validate against
     * @return true if access token is valid, false otherwise
     */
    public boolean isAccessTokenValid(String token, UserDetails userDetails){
        return isTokenValid(token, userDetails);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token to check
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the Claims object containing all token claims
     */
    private Claims extractAllClaims(String token){
        //Initializes the parser builder, then tells the parser what signing key to use, and finally parses the claims from the token, returning the payload
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Decodes and returns the signing key for JWT operations.
     * Converts the base64-encoded secret key to a Key object.
     *
     * @return the signing key
     */
    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the refresh token from an HTTP request's cookies.
     *
     * @param request the HTTP servlet request
     * @return an Optional containing the refresh token if present, empty otherwise
     */
    public Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        return cookie == null ? Optional.empty() : Optional.ofNullable(cookie.getValue());
    }

}
