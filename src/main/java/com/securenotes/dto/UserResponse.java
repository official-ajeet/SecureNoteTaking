package com.securenotes.dto;

import com.securenotes.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private String name;
    private String email;
    private String role;
    private boolean active;

    public static UserResponse to(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setActive(user.isActive());
        return userResponse;
    }
}
