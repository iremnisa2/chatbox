package com.chatbox.domain.service;

import com.chatbox.domain.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    Page<Message> listMessages(Long requesterUserId, Long conversationId, Pageable pageable);

    Message sendMessage(
            Long requesterUserId,
            Long conversationId,
            String content,
            String clientMsgId
    );

    void markDelivered(Long requesterUserId, Long messageId);

    void markRead(Long requesterUserId, Long messageId);
}
