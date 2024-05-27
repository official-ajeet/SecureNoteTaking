package com.securenotes.controller;

import com.securenotes.dto.CreateUserRequest;
import com.securenotes.dto.LoginRequest;
import com.securenotes.dto.LoginResponse;
import com.securenotes.dto.UserResponse;
import com.securenotes.model.User;
import com.securenotes.repository.UserRepository;
import com.securenotes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse>signUp(@RequestBody CreateUserRequest createUserRequest){

        return ResponseEntity.ok(userService.create(createUserRequest));
    }

    @PutMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return ResponseEntity.ok(userService.verifyAccount(email, otp));
    }

    @PutMapping("/regenerate-otp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        return ResponseEntity.ok(userService.regenerateOtp(email));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse>login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.login(loginRequest));
    }



}
