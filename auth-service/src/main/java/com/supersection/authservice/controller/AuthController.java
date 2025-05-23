package com.supersection.authservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.supersection.authservice.dto.LoginRequestDTO;

import io.swagger.v3.oas.annotations.Operation;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.supersection.authservice.dto.LoginResponseDTO;
import com.supersection.authservice.service.AuthService;


@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Generate JWT token on user login")
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> postMethodName(
      @RequestBody LoginRequestDTO loginRequestDTO
  ) {

    Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

    if (tokenOptional.isEmpty()) {
      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .build();
    }

    String token = tokenOptional.get();
    return ResponseEntity.ok(new LoginResponseDTO(token));
  }

}
