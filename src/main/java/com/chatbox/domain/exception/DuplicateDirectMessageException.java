package com.chatbox.domain.exception;

import com.chatbox.domain.model.response.ConversationResponse;

public class DuplicateDirectMessageException extends RuntimeException {

    private final ConversationResponse existing;

    public DuplicateDirectMessageException(String message, ConversationResponse existing) {
        super(message);
        this.existing = existing;
    }

    public ConversationResponse getExisting() {
        return existing;
    }
}