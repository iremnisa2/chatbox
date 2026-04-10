package com.chatbox.domain.repository;

import com.chatbox.domain.model.entity.Message;
import com.chatbox.domain.model.entity.Message.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Conversation message history
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    // Offline sync: unread messages for a user in a conversation
    List<Message> findByConversationIdAndStatusInOrderByCreatedAtAsc(
            Long conversationId,
            List<MessageStatus> statuses
    );

    Optional<Message> findByConversationIdAndSenderIdAndClientMsgId(
            Long conversationId,
            Long senderId,
            String clientMsgId
    );
}
