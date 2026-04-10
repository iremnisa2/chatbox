package com.chatbox.ws;

import com.chatbox.domain.repository.ConversationMemberRepository;
import com.chatbox.ws.dto.WsEnvelope;
import com.chatbox.domain.model.entity.User;
import com.chatbox.domain.repository.UserRepository;
import com.chatbox.domain.repository.ConversationRepository;
import com.chatbox.domain.service.MessageService;
import com.chatbox.ws.session.WsSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WsSessionRegistry sessionRegistry;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ChatWebSocketHandler(
            ObjectMapper objectMapper,
            WsSessionRegistry sessionRegistry,
            MessageService messageService,
            UserRepository userRepository,
            ConversationMemberRepository conversationmemberRepository
    ) {
        this.objectMapper = objectMapper;
        this.sessionRegistry = sessionRegistry;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationmemberRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        Object usernameObj = session.getAttributes().get("username");
        if (!(usernameObj instanceof String username) || username.isBlank()) {
            session.sendMessage(json(WsEnvelope.error("NO_USERNAME_IN_SESSION")));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String number = username;
        User user = userRepository.findByNumber(number).orElse(null);
        if (user == null) {
            session.sendMessage(json(WsEnvelope.error("USER_NOT_FOUND")));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Long userId = user.getId();

        // registry bind
        sessionRegistry.bindUser(session, userId);


        session.sendMessage(json(WsEnvelope.authOk(userId)));
        session.sendMessage(json(WsEnvelope.server("WS_CONNECTED")));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WsEnvelope req;
        try {
            req = objectMapper.readValue(message.getPayload(), WsEnvelope.class);
        } catch (Exception ex) {
            session.sendMessage(json(WsEnvelope.error("INVALID_JSON")));
            return;
        }

        if (req.getType() == null) {
            session.sendMessage(json(WsEnvelope.error("TYPE_REQUIRED")));
            return;
        }

        switch (req.getType()) {

            case AUTH -> {

                Long already = sessionRegistry.getUserId(session);
                if (already != null) {
                    session.sendMessage(json(WsEnvelope.authOk(already)));
                    return;
                }
                session.sendMessage(json(WsEnvelope.error("AUTH_NOT_ALLOWED_USE_TICKET")));
            }

            case CONVERSATION_OPEN -> {
                if (!ensureBound(session)) return;

                Long userId = sessionRegistry.getUserId(session);

                boolean isMember = conversationMemberRepository
                        .existsByConversationIdAndUserId(
                                req.getConversationId(),
                                userId
                        );

                if (!isMember) {
                    session.sendMessage(json(WsEnvelope.error("NOT_A_MEMBER")));
                    return;
                }

                sessionRegistry.setActiveConversation(session, req.getConversationId());

                session.sendMessage(json(
                        WsEnvelope.ack(req.getRequestId(), "CONVERSATION_OPENED")
                ));
            }

            case MESSAGE_SEND -> {
                if (!ensureBound(session)) return;

                Long conversationId = req.getConversationId();
                if (conversationId == null) {
                    session.sendMessage(json(WsEnvelope.error("CONVERSATION_ID_REQUIRED")));
                    return;
                }

                String content = req.getContent();
                if (content == null || content.isBlank()) {
                    session.sendMessage(json(WsEnvelope.error("CONTENT_REQUIRED")));
                    return;
                }

                Long senderUserId = sessionRegistry.getUserId(session);

                var saved = messageService.sendMessage(
                        senderUserId,
                        conversationId,
                        content,
                        req.getClientMsgId()
                );

                session.sendMessage(json(WsEnvelope.messageAck(
                        req.getRequestId(),
                        req.getClientMsgId(),
                        saved.getId(),
                        saved.getConversation().getId(),
                        "SENT"
                )));

                sessionRegistry.broadcastToConversationMembers(
                        conversationId,
                        json(WsEnvelope.messageNew(saved)),
                        senderUserId
                );
            }

            case MESSAGE_DELIVERED -> {
                if (!ensureBound(session)) return;

                if (req.getMessageId() == null) {
                    session.sendMessage(json(WsEnvelope.error("MESSAGE_ID_REQUIRED")));
                    return;
                }

                Long userId = sessionRegistry.getUserId(session);
                messageService.markDelivered(userId, req.getMessageId());

                sessionRegistry.broadcastStatusUpdateByMessageId(
                        req.getMessageId(),
                        json(WsEnvelope.messageStatusUpdate(req.getMessageId(), "DELIVERED")),
                        userId
                );
            }

            case MESSAGE_READ -> {
                if (!ensureBound(session)) return;

                if (req.getMessageId() == null) {
                    session.sendMessage(json(WsEnvelope.error("MESSAGE_ID_REQUIRED")));
                    return;
                }

                Long userId = sessionRegistry.getUserId(session);
                messageService.markRead(userId, req.getMessageId());

                sessionRegistry.broadcastStatusUpdateByMessageId(
                        req.getMessageId(),
                        json(WsEnvelope.messageStatusUpdate(req.getMessageId(), "READ")),
                        userId
                );
            }

            case PING -> session.sendMessage(json(WsEnvelope.pong(req.getRequestId())));

            default -> session.sendMessage(json(WsEnvelope.error("UNKNOWN_TYPE")));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.removeSession(session);
    }

    private boolean ensureBound(WebSocketSession session) throws Exception {
        if (!sessionRegistry.isBound(session)) {
            session.sendMessage(json(WsEnvelope.error("NOT_AUTHENTICATED_GET_TICKET_FIRST")));
            return false;
        }
        return true;
    }

    private TextMessage json(Object obj) throws Exception {
        return new TextMessage(objectMapper.writeValueAsString(obj));
    }
}