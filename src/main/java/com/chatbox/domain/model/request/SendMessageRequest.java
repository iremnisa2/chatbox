package com.chatbox.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull Long conversationId,
        @NotBlank @Size(max = 5000) String content,
        String clientMsgId
) {}
