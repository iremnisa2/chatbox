package com.chatbox.domain.controller;

import com.chatbox.domain.model.request.AddMembersRequest;
import com.chatbox.domain.model.request.CreateConversationRequest;
import com.chatbox.domain.model.response.ConversationDetailResponse;
import com.chatbox.domain.model.response.ConversationResponse;
import com.chatbox.domain.model.response.MemberResponse;
import com.chatbox.domain.service.ConversationService;
import com.chatbox.domain.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    public ConversationController(ConversationService conversationService,
                                  CurrentUserService currentUserService) {
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ConversationResponse createConversation(
            @Valid @RequestBody CreateConversationRequest request
    ) {
        Long userId = currentUserService.getCurrentUserId();
        return conversationService.createConversation(userId, request);
    }

    @GetMapping("/me")
    public List<ConversationResponse> myConversations() {
        Long userId = currentUserService.getCurrentUserId();
        return conversationService.getMyConversations(userId);
    }

    @GetMapping("/{conversationId}")
    public ConversationDetailResponse detail(
            @PathVariable Long conversationId
    ) {
        Long userId = currentUserService.getCurrentUserId();
        return conversationService.getConversationDetail(userId, conversationId);
    }

    @GetMapping("/{conversationId}/members")
    public List<MemberResponse> members(
            @PathVariable Long conversationId
    ) {
        Long userId = currentUserService.getCurrentUserId();
        return conversationService.listMembers(userId, conversationId);
    }

    @PostMapping("/{conversationId}/members")
    public void addMembers(
            @PathVariable Long conversationId,
            @Valid @RequestBody AddMembersRequest request
    ) {
        Long userId = currentUserService.getCurrentUserId();
        conversationService.addMembers(userId, conversationId, request);
    }

    @DeleteMapping("/{conversationId}/members/{memberUserId}")
    public void removeMember(
            @PathVariable Long conversationId,
            @PathVariable Long memberUserId
    ) {
        Long userId = currentUserService.getCurrentUserId();
        conversationService.removeMember(userId, conversationId, memberUserId);
    }
    @DeleteMapping("/{conversationId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable Long conversationId) {
        Long userId = currentUserService.getCurrentUserId();
        conversationService.deleteConversation(userId, conversationId);
    }
}