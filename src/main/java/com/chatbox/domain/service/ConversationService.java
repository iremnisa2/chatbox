package com.chatbox.domain.service;

import com.chatbox.domain.model.request.AddMembersRequest;
import com.chatbox.domain.model.request.CreateConversationRequest;
import com.chatbox.domain.model.response.ConversationDetailResponse;
import com.chatbox.domain.model.response.ConversationResponse;
import com.chatbox.domain.model.response.MemberResponse;

import java.util.List;

public interface ConversationService {

    ConversationResponse createConversation(Long creatorUserId, CreateConversationRequest request);

    List<ConversationResponse> getMyConversations(Long userId);

    ConversationDetailResponse getConversationDetail(Long userId, Long conversationId);

    List<MemberResponse> listMembers(Long userId, Long conversationId);

    void addMembers(Long requesterUserId, Long conversationId, AddMembersRequest request);

    void removeMember(Long requesterUserId, Long conversationId, Long memberUserId);

    void deleteConversation(Long requesterUserId, Long conversationId);
}
