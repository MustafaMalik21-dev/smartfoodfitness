package com.mustafa.smartfoodfitness.service;

import com.mustafa.smartfoodfitness.dto.MessageDto;
import com.mustafa.smartfoodfitness.entity.Message;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.FriendRequestRepository;
import com.mustafa.smartfoodfitness.repository.MessageRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepo;
    private final UserProfileRepository profileRepo;
    private final FriendRequestRepository friendRepo;

    public MessageService(MessageRepository messageRepo,
                          UserProfileRepository profileRepo,
                          FriendRequestRepository friendRepo) {
        this.messageRepo = messageRepo;
        this.profileRepo = profileRepo;
        this.friendRepo  = friendRepo;
    }

    @Transactional
    public MessageDto send(Long senderId, Long receiverId, String content) {
        if (content == null || content.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty.");
        if (content.length() > 2000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message too long.");

        // Only friends can message each other
        friendRepo.findBetween(senderId, receiverId).ifPresentOrElse(
            fr -> { if (!"ACCEPTED".equals(fr.getStatus()))
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not friends."); },
            () -> { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not friends."); }
        );

        UserProfile sender   = profileRepo.findById(senderId)  .orElseThrow();
        UserProfile receiver = profileRepo.findById(receiverId).orElseThrow();

        Message m = new Message();
        m.setSender(sender); m.setReceiver(receiver);
        m.setContent(content.trim());
        m.setSentAt(Instant.now());
        return toDto(messageRepo.save(m));
    }

    @Transactional
    public List<MessageDto> getConversation(Long userA, Long userB) {
        // Mark all messages from B to A as read
        messageRepo.markConversationRead(userB, userA, Instant.now());
        return messageRepo.findConversation(userA, userB).stream()
            .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getInbox(Long userId) {
        return messageRepo.findLatestPerConversation(userId).stream()
            .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return messageRepo.countByReceiverIdAndReadAtIsNull(userId);
    }

    private MessageDto toDto(Message m) {
        MessageDto d = new MessageDto();
        d.setId(m.getId());
        d.setSenderId(m.getSender().getId());
        d.setSenderName(m.getSender().getDisplayName());
        d.setReceiverId(m.getReceiver().getId());
        d.setReceiverName(m.getReceiver().getDisplayName());
        d.setContent(m.getContent());
        d.setSentAt(m.getSentAt());
        d.setReadAt(m.getReadAt());
        return d;
    }
}
