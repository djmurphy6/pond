package com.pond.server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pond.server.model.User;


@Repository
//The extends means that we need findByEmail and findByVerificationCode methods as well as the methods from Crud Repository
//CrudRepository<User, Long>: a repository that works with Users whose ID is a Long.
public interface UserRepository extends CrudRepository<User, UUID>{
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);

    Optional<User> findByUsername(String username);
}

