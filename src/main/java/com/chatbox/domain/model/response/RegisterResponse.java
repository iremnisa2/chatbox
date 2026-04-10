package com.chatbox.domain.model.response;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String firstname,
        String lastname,
        String number,
        LocalDateTime createdAt
) {}