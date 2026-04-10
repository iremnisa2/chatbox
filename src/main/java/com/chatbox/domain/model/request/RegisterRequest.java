package com.chatbox.domain.model.request;

public record RegisterRequest(
        String firstname,
        String lastname,
        String number,
        String password
) {}