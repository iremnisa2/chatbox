package com.chatbox.ws.dto;

import com.chatbox.domain.model.entity.Message;

import java.time.LocalDateTime;

public class WsEnvelope {

    public enum Type {
        // Client -> Server
        AUTH,
        CONVERSATION_OPEN,
        MESSAGE_SEND,
        MESSAGE_DELIVERED,
        MESSAGE_READ,
        PING,

        // Server -> Client
        AUTH_OK,
        AUTH_FAIL,
        MESSAGE_ACK,
        MESSAGE_NEW,
        MESSAGE_STATUS_UPDATE,
        SYNC_MESSAGES,
        SERVER,
        SERVER_ACK,
        SERVER_ERROR,
        PONG
    }

    private Type type;

    // meta
    private String requestId;
    private long ts;

    // routing
    private Long userId;
    private String username;
    private Long conversationId;

    // message send payload
    private String content;
    private String clientMsgId;

    // receipts/status
    private Long messageId;
    private String status;

    // sync / server info
    private String info;
    private MessageDto message;
    private MessageDto[] messages;

    public WsEnvelope() {}

    // (server -> client)

    public static WsEnvelope server(String info) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.SERVER;
        e.info = info;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope ack(String requestId, String info) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.SERVER_ACK;
        e.requestId = requestId;
        e.info = info;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope error(String info) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.SERVER_ERROR;
        e.info = info;
        e.ts = System.currentTimeMillis();
        return e;
    }
    public static WsEnvelope authOk(Long userId) {
        return authOk(userId, null);
    }
    public static WsEnvelope authOk(Long userId, String username) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.AUTH_OK;
        e.userId = userId;
        e.username = username;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope authFail(String reason) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.AUTH_FAIL;
        e.info = reason;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope pong(String requestId) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.PONG;
        e.requestId = requestId;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope messageAck(String requestId,
                                        String clientMsgId,
                                        Long messageId,
                                        Long conversationId,
                                        String status) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.MESSAGE_ACK;
        e.requestId = requestId;
        e.clientMsgId = clientMsgId;
        e.messageId = messageId;
        e.conversationId = conversationId;
        e.status = status;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope messageNew(Message saved) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.MESSAGE_NEW;
        e.conversationId = saved.getConversation().getId();
        e.message = MessageDto.from(saved);
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope messageStatusUpdate(Long messageId, String status) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.MESSAGE_STATUS_UPDATE;
        e.messageId = messageId;
        e.status = status;
        e.ts = System.currentTimeMillis();
        return e;
    }

    public static WsEnvelope syncMessages(Long conversationId, MessageDto[] messages) {
        WsEnvelope e = new WsEnvelope();
        e.type = Type.SYNC_MESSAGES;
        e.conversationId = conversationId;
        e.messages = messages;
        e.ts = System.currentTimeMillis();
        return e;
    }

    //dto

    public static class MessageDto {
        private Long id;
        private Long conversationId;
        private Long senderId;
        private String content;
        private String status;
        private String createdAt;

        public MessageDto() {}

        public static MessageDto from(Message m) {
            MessageDto d = new MessageDto();
            d.id = m.getId();
            d.conversationId = m.getConversation().getId();
            d.senderId = m.getSender().getId();
            d.content = m.getContent();
            d.status = m.getStatus() != null ? m.getStatus().name() : null;
            d.createdAt = toIso(m.getCreatedAt());
            return d;
        }

        private static String toIso(LocalDateTime t) {
            return t == null ? null : t.toString();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    //getters/setters

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getClientMsgId() { return clientMsgId; }
    public void setClientMsgId(String clientMsgId) { this.clientMsgId = clientMsgId; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public MessageDto getMessage() { return message; }
    public void setMessage(MessageDto message) { this.message = message; }

    public MessageDto[] getMessages() { return messages; }
    public void setMessages(MessageDto[] messages) { this.messages = messages; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
