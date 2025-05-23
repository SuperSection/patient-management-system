package com.supersection.authservice.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.supersection.authservice.model.User;
import com.supersection.authservice.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }
}
