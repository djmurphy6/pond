package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.LoginUserDTO;
import com.pond.server.dto.RegisterUserDTO;
import com.pond.server.dto.VerifyUserDTO;
import com.pond.server.model.User;
import com.pond.server.repository.UserRepository;

/**
 * Service class for handling user authentication operations.
 * Manages user registration, login, and email verification processes.
 */
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Constructs a new AuthenticationService with required dependencies.
     *
     * @param userRepository the repository for user data access
     * @param passwordEncoder the encoder for password hashing
     * @param authenticationManager the Spring Security authentication manager
     * @param emailService the service for sending emails
     */
    public AuthenticationService (
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    /**
     * Registers a new user account.
     * Creates a user with hashed password, generates a verification code,
     * and sends a verification email. The account starts in disabled state until verified.
     *
     * @param input the registration data containing username, email, and password
     * @return the newly created User entity (in disabled state)
     * @throws RuntimeException if a user with the given email already exists
     */
    @Transactional
    public User signup(RegisterUserDTO input){
        Optional<User> existingUser = userRepository.findByEmail(input.getEmail()) ;
        if (existingUser.isPresent()){
            throw new RuntimeException("User already exists");
        }
        
        // Validate that email is from University of Oregon
        if (!input.getEmail().toLowerCase().endsWith("@uoregon.edu")) {
            throw new RuntimeException("Only @uoregon.edu email addresses are allowed");
        }
        
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        
        return savedUser;
    }

    /**
     * Authenticates a user login attempt.
     * Validates that the user exists, is verified, and credentials are correct.
     *
     * @param input the login credentials containing email and password
     * @return the authenticated User entity
     * @throws RuntimeException if user not found, account not verified, or password is invalid
     */
    @Transactional(readOnly = true)
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

    /**
     * Verifies a user's email address using the verification code.
     * Enables the user account if the code is valid and not expired.
     *
     * @param input the verification data containing email and verification code
     * @throws RuntimeException if user not found, no verification code exists,
     *                         verification code expired, or code is invalid
     */
    @Transactional
    public void verifyUser(VerifyUserDTO input){
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            
            // Check if verification code exists
            if (user.getVerificationCode() == null) {
                throw new RuntimeException("No verification code found. Please register again.");
            }
            
            // Check if expiration exists and if code is expired
            if (user.getVerificationCodeExpiration() == null || 
                user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code has expired!");
            }
            
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiration(null);
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

    /**
     * Generates a random 6-digit verification code.
     *
     * @return a string containing a 6-digit numeric code
     */
    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


}