package com.nexcare.backend.controller;

import com.nexcare.backend.dto.LoginRequest;
import com.nexcare.backend.dto.LoginResponseDTO;
import com.nexcare.backend.dto.SignupRequest;
import com.nexcare.backend.dto.UserResponseDTO;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody SignupRequest request){
        User savedUser = userService.registerUser(request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(
            @Valid @RequestBody LoginRequest request) {

        String token = userService.loginUser(request);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}

