package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pond.server.dto.LoginUserDTO;
import com.pond.server.dto.RegisterUserDTO;
import com.pond.server.dto.VerifyUserDTO;
import com.pond.server.model.User;
import com.pond.server.repository.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService (
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signup(RegisterUserDTO input){
        Optional<User> existingUser = userRepository.findByEmail(input.getEmail()) ;
        if (existingUser.isPresent()){
            throw new RuntimeException("User already exists");
        }
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        return userRepository.save(user);
    }

    public User authentication(LoginUserDTO input){
        User user =userRepository.findByEmail(input.getEmail()).orElseThrow(()->new RuntimeException("User not found: invalid email"));
        if (!user.isEnabled()){
            throw new RuntimeException("Account not verified. Please verify your account");
        }
       try {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    input.getEmail(),
                    input.getPassword()
            )
    );
       } catch (BadCredentialsException e) {
        throw new RuntimeException("Invalid password");
       }
        return user;
    }

    public void verifyUser(VerifyUserDTO input){
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code has expired!");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
//                user.getVerificationCodeExpiration();
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");

            }
        } else {
            throw new RuntimeException("User not found");
        }
    }
    // Optional for the email verification step not needed yet
//    public void resendVerificationCode(String email)

    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


}