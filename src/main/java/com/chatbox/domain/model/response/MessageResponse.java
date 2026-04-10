package com.chatbox.domain.model.response;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        UserResponse sender,
        String content,
        String status,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt,
        LocalDateTime readAt,
        String clientMsgId
) {}
