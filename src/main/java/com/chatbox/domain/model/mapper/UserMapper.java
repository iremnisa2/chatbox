package com.chatbox.domain.model.mapper;

import com.chatbox.domain.model.response.UserResponse;
import com.chatbox.domain.model.entity.User;

public class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getNumber()
        );
    }
}
