package com.chatbox.ws.session;

import com.chatbox.domain.model.entity.Message;
import com.chatbox.domain.repository.ConversationMemberRepository;
import com.chatbox.domain.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsSessionRegistry {

    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;

    // sessionId -> session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // sessionId -> userId
    private final Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();

    // userId -> sessionIds
    private final Map<Long, Set<String>> userIdToSessionIds = new ConcurrentHashMap<>();

    // sessionId -> active conversation
    private final Map<String, Long> sessionIdToActiveConversationId = new ConcurrentHashMap<>();

    public WsSessionRegistry(
            ConversationMemberRepository conversationMemberRepository,
            MessageRepository messageRepository
    ) {
        this.conversationMemberRepository = conversationMemberRepository;
        this.messageRepository = messageRepository;
    }

    //binding

    public void bindUser(WebSocketSession session, Long userId) {
        removeSession(session);

        sessions.put(session.getId(), session);
        sessionIdToUserId.put(session.getId(), userId);

        userIdToSessionIds
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session.getId());
    }
    public Long getUserId(WebSocketSession session) {
        return sessionIdToUserId.get(session.getId());
    }

    public boolean isBound(WebSocketSession session) {
        return sessionIdToUserId.containsKey(session.getId());
    }

    public void setActiveConversation(WebSocketSession session, Long conversationId) {
        sessionIdToActiveConversationId.put(session.getId(), conversationId);
    }

    public Long getActiveConversationId(WebSocketSession session) {
        return sessionIdToActiveConversationId.get(session.getId());
    }

    public void broadcastToConversationMembers(Long conversationId, TextMessage msg, Long excludeUserId) {
        List<Long> memberUserIds = conversationMemberRepository.findUserIdsByConversationId(conversationId);
        if (memberUserIds == null || memberUserIds.isEmpty()) return;

        for (Long userId : memberUserIds) {
            if (excludeUserId != null && excludeUserId.equals(userId)) continue;
            sendToUser(userId, msg);
        }
    }

    public void sendToUser(Long userId, TextMessage msg) {
        Set<String> ids = userIdToSessionIds.get(userId);
        if (ids == null || ids.isEmpty()) return;

        for (String sessionId : ids) {
            WebSocketSession s = sessions.get(sessionId);
            try {
                if (s != null && s.isOpen()) s.sendMessage(msg);
            } catch (Exception ignored) {}
        }
    }

    public void broadcastStatusUpdateByMessageId(Long messageId, TextMessage msg, Long actorUserId) {
        Optional<Message> opt = messageRepository.findById(messageId);
        if (opt.isEmpty()) return;

        Message m = opt.get();
        Long conversationId = m.getConversation().getId();
        Long senderId = m.getSender().getId();

        // Always notify sender (all devices)
        sendToUser(senderId, msg);

        // Also notify other members (except actor)
        List<Long> memberUserIds = conversationMemberRepository.findUserIdsByConversationId(conversationId);
        if (memberUserIds == null) return;

        for (Long userId : memberUserIds) {
            if (Objects.equals(userId, senderId)) continue;
            if (Objects.equals(userId, actorUserId)) continue;
            sendToUser(userId, msg);
        }
    }

    //cleanup

    public void removeSession(WebSocketSession session) {
        String sid = session.getId();

        sessions.remove(sid);

        Long userId = sessionIdToUserId.remove(sid);
        if (userId != null) {
            Set<String> set = userIdToSessionIds.get(userId);
            if (set != null) {
                set.remove(sid);
                if (set.isEmpty()) userIdToSessionIds.remove(userId);
            }
        }

        sessionIdToActiveConversationId.remove(sid);
    }
}
