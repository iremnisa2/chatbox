package com.chatbox.domain.service.impl;

import com.chatbox.domain.model.entity.Conversation;
import com.chatbox.domain.model.entity.Message;
import com.chatbox.domain.model.entity.Message.MessageStatus;
import com.chatbox.domain.model.entity.User;
import com.chatbox.domain.repository.ConversationMemberRepository;
import com.chatbox.domain.repository.ConversationRepository;
import com.chatbox.domain.repository.MessageRepository;
import com.chatbox.domain.repository.UserRepository;
import com.chatbox.domain.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Message> listMessages(Long requesterUserId, Long conversationId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Conversation not found: " + conversationId));

        ensureMember(requesterUserId, conversationId);

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    @Override
    @Transactional
    public Message sendMessage(Long requesterUserId,
                               Long conversationId,
                               String content,
                               String clientMsgId) {

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Message content cannot be empty");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Conversation not found: " + conversationId));

        User sender = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + requesterUserId));

        ensureMember(requesterUserId, conversationId);

        // Deduplication for RAW WebSocket retries
        if (clientMsgId != null) {
            return messageRepository
                    .findByConversationIdAndSenderIdAndClientMsgId(conversationId, requesterUserId, clientMsgId)
                    .orElseGet(() -> persistMessage(conversation, sender, content, clientMsgId));
        }

        return persistMessage(conversation, sender, content, null);
    }

    @Override
    @Transactional
    public void markDelivered(Long requesterUserId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found: " + messageId));

        ensureMember(requesterUserId, message.getConversation().getId());

        if (message.getStatus() == MessageStatus.SENT) {
            message.setStatus(MessageStatus.DELIVERED);
            message.setDeliveredAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void markRead(Long requesterUserId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found: " + messageId));

        ensureMember(requesterUserId, message.getConversation().getId());

        if (message.getStatus() != MessageStatus.READ) {
            message.setStatus(MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
        }
    }

    private Message persistMessage(Conversation conversation,
                                   User sender,
                                   String content,
                                   String clientMsgId) {

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content.trim())
                .clientMsgId(clientMsgId)
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    private void ensureMember(Long userId, Long conversationId) {
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            throw new ResponseStatusException(FORBIDDEN, "You are not a member of this conversation");
        }
    }
}
