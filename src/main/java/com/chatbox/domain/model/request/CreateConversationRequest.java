package com.chatbox.domain.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateConversationRequest(
        @Size(max = 255)
        String title,

        @NotNull
        Boolean isGroup,

        @NotNull
        List<Long> memberUserIds
) {}
