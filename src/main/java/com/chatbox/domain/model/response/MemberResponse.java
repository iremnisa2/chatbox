package com.chatbox.domain.model.response;

import com.chatbox.domain.model.entity.ConversationRole;
import java.time.LocalDateTime;

public record MemberResponse(
        Long userId,
        String firstname,
        String lastname,
        String number,
        ConversationRole role,
        LocalDateTime joinedAt
) {}
