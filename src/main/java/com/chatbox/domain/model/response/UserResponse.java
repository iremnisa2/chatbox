package com.chatbox.domain.model.response;

public record UserResponse(
        Long id,
        String firstname,
        String lastname,
        String number
) {}
