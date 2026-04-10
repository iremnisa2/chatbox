package com.chatbox.domain.repository;

import com.chatbox.domain.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
