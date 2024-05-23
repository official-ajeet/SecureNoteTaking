package com.securenotes.service;
import com.securenotes.dto.CreateUserRequest;
import com.securenotes.dto.LoginRequest;
import com.securenotes.exceptions.EmailAlreadyExistsException;
import com.securenotes.model.Token;
import com.securenotes.model.User;
import com.securenotes.repository.NotesRepository;
import com.securenotes.repository.TokenRepository;
import com.securenotes.repository.UserRepository;
import com.securenotes.utils.EmailUtil;
import com.securenotes.utils.JWTUtils;
import com.securenotes.utils.OtpUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService {
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotesRepository notesRepository;

    @Autowired
    OurUserDetailService ourUserDetailService;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    TokenRepository tokenRepository;

    public User create(CreateUserRequest createUserRequest){
        User existingUser = userRepository.findByEmail(createUserRequest.getEmail());
        if(existingUser != null){
            throw new EmailAlreadyExistsException("User with email " + createUserRequest.getEmail() + " already exists"); // You can define a custom exception for this
        }

        //otp related stuff
        String otp = otpUtil.generateOtp();
        try{
            emailUtil.sendOtpEmail(createUserRequest.getEmail(),otp);
        }catch (MessagingException e){
            throw new RuntimeException("Unable to send the otp, Please try again!");
        }


        User user = new User();
        user.setEmail(createUserRequest.getEmail());
        user.setName(createUserRequest.getName());
        user.setRole(createUserRequest.getRole());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setOtp(passwordEncoder.encode(otp));
        user.setOtpGenerationTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserById(int id){
        return userRepository.findById(id);
    }


    public LoginRequest login(LoginRequest loginRequest) {

        LoginRequest loginResponse = new LoginRequest();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        var user = userRepository.findByEmail(loginRequest.getEmail());
        var jwt = jwtUtils.generateToken(user);
        var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        if(!user.isActive()){
            throw new RuntimeException("User is not verified yet, Please verify your email...");
        }

        //revoke all tokens for a user
        revokeAllTokensByUser(user);
        //save token to db
        saveUserToken(user, jwt, refreshToken);

        loginResponse.setToken(jwt);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setRole(user.getRole());

        return  loginResponse;
    }

    private void saveUserToken(User user, String jwt, String refreshToken) {
        Token token = new Token();
        token.setToken(jwt);
        token.setRefreshToken(refreshToken);
        token.setUser(user);
        token.setLoggedOut(false);
        tokenRepository.save(token);
    }

    private void revokeAllTokensByUser(User user){
        List<Token>validTokensListByUser = tokenRepository.findAllTokenByUser(user.getUserId());
        if(!validTokensListByUser.isEmpty()){
            validTokensListByUser.forEach(t -> {
                t.setLoggedOut(true);
            });
        }
        tokenRepository.saveAll(validTokensListByUser);
    }

    //verification related
    public String verifyAccount(String email, String otp) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            return "User not found with this email: "+email;
        }
        //checking otp from saved otp with entered otp

        if (passwordEncoder.matches(otp,user.getOtp())&& Duration.between(user.getOtpGenerationTime(),
                LocalDateTime.now()).getSeconds() < (1 * 60)) {//checking time of otp
            user.setActive(true);
            userRepository.save(user);
            return "OTP verified you can login";
        }
        return "Please regenerate otp and try again";
    }

    public String regenerateOtp(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            return "User not found with this email: "+email;
        }

        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        user.setOtp(passwordEncoder.encode(otp));
        user.setOtpGenerationTime(LocalDateTime.now());
        userRepository.save(user);
        return "Email sent... please verify account within 1 minute";
    }


}
