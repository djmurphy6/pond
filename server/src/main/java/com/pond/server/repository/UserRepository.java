package com.pond.server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pond.server.model.User;


/**
 * Repository interface for {@link User} entity database operations.
 * 
 * <p>Extends Spring Data's {@link CrudRepository} to provide standard CRUD operations
 * for User entities identified by UUID. Additional custom query methods support
 * authentication and user lookup by various unique identifiers.</p>
 * 
 * <p>This repository is primarily used by authentication services for login,
 * registration, and email verification workflows.</p>
 * 
 * @author Pond Team
 * @see User
 * @see org.springframework.data.repository.CrudRepository
 */
@Repository
public interface UserRepository extends CrudRepository<User, UUID>{
    
    /**
     * Finds a user by their email address.
     * Used during authentication and registration to check if email exists.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds a user by their verification code.
     * Used during the email verification process to activate new accounts.
     * 
     * @param verificationCode the verification code sent to user's email
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByVerificationCode(String verificationCode);

    /**
     * Finds a user by their username.
     * Used to check username availability during registration and for user lookup.
     * 
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
}

