package com.securenotes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.securenotes.model.User;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String name;
    private String email;
    private String role;
    private Boolean active;
    private String message;
    private User user;

    public static UserResponse to(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setActive(user.isActive());
        return userResponse;
    }
}
