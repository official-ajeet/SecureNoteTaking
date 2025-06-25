package com.securenotes.controller;

import com.securenotes.dto.CreateUserRequest;
import com.securenotes.dto.LoginRequest;
import com.securenotes.dto.LoginResponse;
import com.securenotes.dto.UserResponse;
import com.securenotes.model.User;
import com.securenotes.repository.UserRepository;
import com.securenotes.service.UserService;
import com.securenotes.utils.EncryptionUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse>signUp(@RequestBody CreateUserRequest createUserRequest) throws Exception {
        String encryptedEmail = EncryptionUtil.encrypt(createUserRequest.getEmail());
        User existingUser = userRepository.findByEmail(encryptedEmail);
        if(existingUser != null){
            UserResponse userResponse = new UserResponse();
            userResponse.setMessage("Email Already Exists!");
            return ResponseEntity.badRequest().body(userResponse);
        }

        return ResponseEntity.ok(userService.create(createUserRequest));
    }

    @PutMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) throws Exception {

        return ResponseEntity.ok(userService.verifyAccount(email, otp));
    }

    @PutMapping("/regenerate-otp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) throws Exception {
        return ResponseEntity.ok(userService.regenerateOtp(email));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            // Encrypt the email for lookup
            String encryptedEmail = EncryptionUtil.encrypt(loginRequest.getEmail().toLowerCase());
            User user = userRepository.findByEmail(encryptedEmail);

            // Check if the user is active
            if (!user.isActive()) {
                loginResponse.setMessage("Email is not verified yet, please verify and retry!");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
            }

            // Authenticate user credentials
            authenticateUser(encryptedEmail, loginRequest.getPassword());

            // Process login and return response
            return ResponseEntity.ok(userService.login(loginRequest));
        } catch (AuthenticationException e) {
            loginResponse.setMessage("Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
        } catch (Exception e) {

            loginResponse.setMessage("An unexpected error occurred. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(loginResponse);
        }
    }

    private void authenticateUser(String email, String password) throws AuthenticationException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse>login(@RequestBody LoginRequest loginRequest) throws Exception {
//        String encryptedEmail = EncryptionUtil.encrypt(loginRequest.getEmail());
//
//        LoginResponse loginResponse = new LoginResponse();
//
//        User user  = userRepository.findByEmail(encryptedEmail);
//        if(!user.isActive()){
//            loginResponse.setMessage("Email is not verified yet, Please verify and retry!");
//            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(loginResponse);
//        }
//        try{
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(encryptedEmail,loginRequest.getPassword()));
//        }catch (AuthenticationException e){
//            loginResponse.setMessage("Wrong email or password");
//            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(loginResponse);
//        }
//        return ResponseEntity.ok(userService.login(loginRequest));
//    }

}
