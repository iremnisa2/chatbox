package com.chatbox.domain.controller;

import com.chatbox.domain.model.entity.Message;
import com.chatbox.domain.model.request.SendMessageRequest;
import com.chatbox.domain.model.response.MessageResponse;
import com.chatbox.domain.model.response.UserResponse;
import com.chatbox.domain.service.CurrentUserService;
import com.chatbox.domain.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final CurrentUserService currentUserService;

    // GET /api/messages?conversationId=1&page=0&size=20
    @GetMapping
    public Page<MessageResponse> list(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = currentUserService.getCurrentUserId();
        Page<Message> messages = messageService.listMessages(userId, conversationId, PageRequest.of(page, size));
        return messages.map(this::toResponse);
    }

    // POST /api/messages
    @PostMapping
    public MessageResponse send(
            @Valid @RequestBody SendMessageRequest request
    ) {
        Long userId = currentUserService.getCurrentUserId();
        Message saved = messageService.sendMessage(
                userId,
                request.conversationId(),
                request.content(),
                request.clientMsgId()
        );
        return toResponse(saved);
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation().getId(),
                new UserResponse(
                        m.getSender().getId(),
                        m.getSender().getFirstname(),
                        m.getSender().getLastname(),
                        m.getSender().getNumber()
                ),
                m.getContent(),
                m.getStatus() != null ? m.getStatus().name() : null,
                m.getCreatedAt(),
                m.getDeliveredAt(),
                m.getReadAt(),
                m.getClientMsgId()
        );
    }
}