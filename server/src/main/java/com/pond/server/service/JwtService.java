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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

//Claims are part of the JWT token's payload
 //Claims::getSubject: the subject of the token, which is the username(equivalent to Claims c -> c.getSubject() in Java)
 //T is the generic type in java, lets you reuse one method to extract any claim type from the token

@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.access-expiration-time}")
    private long accessExpiration;

   
    //Pass in a token and the function extracts the username from the token payload
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //Function<Claims, T> claimsResolver: a function that takes a Claims object and returns a T object
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        //Claims object is the payload of the token, final means it cannot be changed once set
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //Overloaded method, if no extra claims are needed, just pass in a empty hashmap to the other generateToken
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    //Overloaded method, if extra claims are needed, pass in the extraClaims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateAccessToken(UserDetails userDetails){
        return buildToken(new HashMap<>(), userDetails, accessExpiration);
    }

    public long getExpirationTime(){
        return jwtExpiration;
    }

    public long getAccessExpirationTime(){
        return accessExpiration;
    }

    //Build token builds a hashmap of the extra claims and the user details, and then builds the token
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


    //Makes sure the token username matches the userDetails username and that the token is not expired
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails){
        return isTokenValid(token, userDetails);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails){
        return isTokenValid(token, userDetails);
    }

    //Makes sure the token is not expired
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    //Extracts the expiration date from the token
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    //Extracts all the claims from the token
    private Claims extractAllClaims(String token){
        //Initializes the parser builder, then tells the parser what signing key to use, and finally parses the claims from the token, returning the payload
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    //Decodes the secret key from base64 to a key
    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        return cookie == null ? Optional.empty() : Optional.ofNullable(cookie.getValue());
    }

}
