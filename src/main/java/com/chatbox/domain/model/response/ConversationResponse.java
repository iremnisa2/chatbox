package com.chatbox.domain.model.response;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String title,
        Boolean isGroup,
        UserResponse createdBy,
        LocalDateTime createdAt,
        int memberCount,
        String otherUserName,
        Boolean isCurrentUserAdmin
) {}