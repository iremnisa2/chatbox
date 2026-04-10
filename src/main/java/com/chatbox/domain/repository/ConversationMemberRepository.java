package com.chatbox.domain.repository;

import com.chatbox.domain.model.entity.Conversation;
import com.chatbox.domain.model.entity.ConversationMember;
import com.chatbox.domain.model.entity.ConversationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationMember> findByConversationId(Long conversationId);

    @Query("""
           select cm.role
           from ConversationMember cm
           where cm.conversation.id = :conversationId
             and cm.user.id = :userId
           """)
    Optional<ConversationRole> findRoleOfUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    @Query("""
           select cm.conversation
           from ConversationMember cm
           where cm.user.id = :userId
           order by cm.conversation.createdAt desc
           """)
    List<Conversation> findConversationsOfUser(@Param("userId") Long userId);


    @Query("""
           select cm.user.id
           from ConversationMember cm
           where cm.conversation.id = :conversationId
           """)
    List<Long> findUserIdsByConversationId(@Param("conversationId") Long conversationId);

    @Query("""
       select cm1.conversation
       from ConversationMember cm1
       join ConversationMember cm2 on cm2.conversation = cm1.conversation
       where cm1.user.id = :userId1
         and cm2.user.id = :userId2
         and cm1.conversation.isGroup = false
         and (select count(cm3) from ConversationMember cm3 where cm3.conversation = cm1.conversation) = 2
       """)
    List<Conversation> findDirectConversationBetween(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}
