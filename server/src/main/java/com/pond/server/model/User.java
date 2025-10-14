package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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



// TODO: Annotate and get class ready for JPA and ensure email is immutable
@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String avatar_url;

    private boolean enabled;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiration;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User() {
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

