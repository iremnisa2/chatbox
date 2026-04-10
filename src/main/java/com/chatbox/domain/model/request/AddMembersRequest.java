package com.chatbox.domain.model.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddMembersRequest(
        @NotNull
        List<Long> userIds
) {}
