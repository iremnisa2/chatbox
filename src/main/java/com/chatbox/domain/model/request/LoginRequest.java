package com.chatbox.domain.model.request;

public record LoginRequest(
        String number,
        String password
) {}