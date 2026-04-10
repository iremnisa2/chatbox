package com.chatbox.domain.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String title,
        Boolean isGroup,
        UserResponse createdBy,
        LocalDateTime createdAt,
        List<MemberResponse> members
) {}
