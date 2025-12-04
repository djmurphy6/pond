package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a user in the Pond marketplace system.
 * 
 * <p>This class implements Spring Security's {@link UserDetails} interface to provide
 * authentication and authorization functionality. Users can create listings, leave reviews,
 * follow other users, and engage in messaging.</p>
 * 
 * <p>Email verification is required for account activation. The verification code
 * has a time-based expiration.</p>
 * 
 * @author Pond Team
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {

    /**
     * Unique identifier for the user (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID UserGU;

    /**
     * Optional biographical information about the user.
     * Displayed on the user's profile page.
     */
    @Column(nullable = true)
    private String bio;

    /**
     * Flag indicating whether the user has administrative privileges.
     * Admins can review reports and manage listings.
     * Defaults to false for new users.
     */
    @Column(nullable = false)
    private Boolean admin = false;

    /**
     * Unique username for the user.
     * Used for display and identification throughout the application.
     */
    @Column(unique = true, nullable = false)
    private String username;
    
    /**
     * Unique email address for the user.
     * Used for authentication and email verification.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Encrypted password for authentication.
     * Stored as a BCrypt hash.
     */
    @Column(nullable = false)
    private String password;

    /**
     * URL to the user's avatar image.
     * Can be null if no avatar is set.
     */
    @Column(nullable = true)
    private String avatar_url;

    /**
     * Flag indicating whether the user's account is enabled.
     * Set to true after successful email verification.
     */
    private boolean enabled;

    /**
     * Verification code sent to user's email for account activation.
     * Single-use code that expires after a set time period.
     */
    @Column(name = "verification_code")
    private String verificationCode;

    /**
     * Timestamp when the verification code expires.
     * After this time, a new code must be generated.
     */
    @Column(name = "verification_code_expiration")
    private LocalDateTime verificationCodeExpiration;

    /**
     * Constructs a new User with the specified username, email, and password.
     * 
     * @param username the username for the new user
     * @param email the email address for the new user
     * @param password the password for the new user (will be encrypted)
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Default constructor for JPA.
     */
    public User() {
    }

    @Override
    public String getUsername(){
        return username;
    }

    

    //TODO: implement 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of();
    }

    //TODO: implement
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    //TODO: implement
    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    //TODO: implement
    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    //TODO: implement
    @Override
    public boolean isEnabled(){
        return enabled;
    }
    
}

