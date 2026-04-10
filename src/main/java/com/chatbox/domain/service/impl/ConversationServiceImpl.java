package com.chatbox.domain.service.impl;

import com.chatbox.domain.exception.DuplicateDirectMessageException;
import com.chatbox.domain.exception.ForbiddenException;
import com.chatbox.domain.exception.ResourceNotFoundException;
import com.chatbox.domain.model.entity.Conversation;
import com.chatbox.domain.model.entity.ConversationMember;
import com.chatbox.domain.model.entity.ConversationRole;
import com.chatbox.domain.model.entity.User;
import com.chatbox.domain.model.mapper.UserMapper;
import com.chatbox.domain.model.request.AddMembersRequest;
import com.chatbox.domain.model.request.CreateConversationRequest;
import com.chatbox.domain.model.response.ConversationDetailResponse;
import com.chatbox.domain.model.response.ConversationResponse;
import com.chatbox.domain.model.response.MemberResponse;
import com.chatbox.domain.model.response.UserResponse;
import com.chatbox.domain.repository.ConversationMemberRepository;
import com.chatbox.domain.repository.ConversationRepository;
import com.chatbox.domain.repository.UserRepository;
import com.chatbox.domain.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ConversationServiceImpl(UserRepository userRepository,
                                   ConversationRepository conversationRepository,
                                   ConversationMemberRepository conversationMemberRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Override
    public ConversationResponse createConversation(Long creatorUserId, CreateConversationRequest request) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorUserId));

        Set<Long> uniqueMemberIds = new HashSet<>();
        if (request.memberUserIds() != null) uniqueMemberIds.addAll(request.memberUserIds());
        uniqueMemberIds.add(creatorUserId);

        boolean isGroup = Boolean.TRUE.equals(request.isGroup());

        // Duplicate DM check
        if (!isGroup && uniqueMemberIds.size() == 2) {
            Long otherUserId = uniqueMemberIds.stream()
                    .filter(id -> !id.equals(creatorUserId))
                    .findFirst()
                    .orElseThrow();

            List<Conversation> existing =
                    conversationMemberRepository.findDirectConversationBetween(creatorUserId, otherUserId);

            if (!existing.isEmpty()) {
                Conversation conv = existing.get(0);
                List<ConversationMember> members = conversationMemberRepository.findByConversationId(conv.getId());
                throw new DuplicateDirectMessageException(
                        "You already have a conversation with this user.",
                        toConversationResponseForUser(conv, members, creatorUserId)
                );
            }
        }

        List<User> members = userRepository.findAllById(uniqueMemberIds);
        if (members.size() != uniqueMemberIds.size()) {
            throw new ResourceNotFoundException("One or more users not found in member list.");
        }

        Conversation conversation = Conversation.builder()
                .title(request.title())
                .isGroup(isGroup)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .build();

        conversation = conversationRepository.save(conversation);

        conversationMemberRepository.save(ConversationMember.builder()
                .conversation(conversation)
                .user(creator)
                .role(ConversationRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build());

        for (User u : members) {
            if (u.getId().equals(creatorUserId)) continue;
            conversationMemberRepository.save(ConversationMember.builder()
                    .conversation(conversation)
                    .user(u)
                    .role(ConversationRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        List<ConversationMember> savedMembers = conversationMemberRepository.findByConversationId(conversation.getId());
        return toConversationResponseForUser(conversation, savedMembers, creatorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long userId) {
        List<Conversation> conversations = conversationMemberRepository.findConversationsOfUser(userId);
        return conversations.stream()
                .map(c -> {
                    List<ConversationMember> members = conversationMemberRepository.findByConversationId(c.getId());
                    return toConversationResponseForUser(c, members, userId);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(Long userId, Long conversationId) {
        ensureMember(userId, conversationId);
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        List<MemberResponse> members = listMembers(userId, conversationId);
        return new ConversationDetailResponse(
                c.getId(), c.getTitle(), c.getIsGroup(),
                UserMapper.toResponse(c.getCreatedBy()), c.getCreatedAt(), members
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long userId, Long conversationId) {
        ensureMember(userId, conversationId);
        return conversationMemberRepository.findByConversationId(conversationId)
                .stream()
                .map(cm -> new MemberResponse(
                        cm.getUser().getId(),
                        cm.getUser().getFirstname(),
                        cm.getUser().getLastname(),
                        cm.getUser().getNumber(),
                        cm.getRole(),
                        cm.getJoinedAt()
                ))
                .toList();
    }

    @Override
    public void addMembers(Long requesterUserId, Long conversationId, AddMembersRequest request) {
        ensureAdmin(requesterUserId, conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        Set<Long> uniqueIds = new HashSet<>(request.userIds());
        if (uniqueIds.isEmpty()) return;
        List<User> usersToAdd = userRepository.findAllById(uniqueIds);
        if (usersToAdd.size() != uniqueIds.size()) throw new ResourceNotFoundException("One or more users not found.");
        for (User u : usersToAdd) {
            if (conversationMemberRepository.existsByConversationIdAndUserId(conversationId, u.getId())) continue;
            conversationMemberRepository.save(ConversationMember.builder()
                    .conversation(conversation).user(u)
                    .role(ConversationRole.MEMBER).joinedAt(LocalDateTime.now()).build());
        }
    }

    @Override
    public void removeMember(Long requesterUserId, Long conversationId, Long memberUserId) {
        ensureAdmin(requesterUserId, conversationId);
        ConversationMember cm = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this conversation."));
        if (cm.getRole() == ConversationRole.ADMIN) throw new ForbiddenException("You cannot remove an ADMIN.");
        conversationMemberRepository.delete(cm);
    }

    @Override
    public void deleteConversation(Long requesterUserId, Long conversationId) {
        ensureAdmin(requesterUserId, conversationId);
        conversationMemberRepository.deleteAll(
                conversationMemberRepository.findByConversationId(conversationId));
        conversationRepository.deleteById(conversationId);
    }

    private void ensureMember(Long userId, Long conversationId) {
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId))
            throw new ForbiddenException("You are not a member of this conversation.");
    }

    private void ensureAdmin(Long userId, Long conversationId) {
        var roleOpt = conversationMemberRepository.findRoleOfUser(conversationId, userId);
        if (roleOpt.isEmpty() || roleOpt.get() != ConversationRole.ADMIN)
            throw new ForbiddenException("Only ADMIN can perform this action.");
    }

    private ConversationResponse toConversationResponseForUser(Conversation c, List<ConversationMember> members, Long currentUserId) {
        boolean isAdmin = members.stream()
                .anyMatch(m -> m.getUser().getId().equals(currentUserId) && m.getRole() == ConversationRole.ADMIN);

        String otherUserName = null;
        if (!Boolean.TRUE.equals(c.getIsGroup())) {
            otherUserName = members.stream()
                    .filter(m -> !m.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .map(m -> (m.getUser().getFirstname() + " " + m.getUser().getLastname()).trim())
                    .orElse(null);
        }

        return new ConversationResponse(
                c.getId(), c.getTitle(), c.getIsGroup(),
                UserMapper.toResponse(c.getCreatedBy()), c.getCreatedAt(),
                members.size(), otherUserName, isAdmin
        );
    }
}