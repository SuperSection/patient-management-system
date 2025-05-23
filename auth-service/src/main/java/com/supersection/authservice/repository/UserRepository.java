package com.supersection.authservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supersection.authservice.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  /**
   * Find a user by their email address.
   *
   * @param email the email address of the user
   * @return an Optional containing the User if found, or empty if not found
   */
  Optional<User> findByEmail(String email);
}
