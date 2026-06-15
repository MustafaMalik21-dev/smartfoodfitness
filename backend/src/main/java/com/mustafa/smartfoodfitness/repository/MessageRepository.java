package com.mustafa.smartfoodfitness.repository;

import com.mustafa.smartfoodfitness.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Full conversation between two users, oldest first */
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :a AND m.receiver.id = :b) OR (m.sender.id = :b AND m.receiver.id = :a) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("a") Long userA, @Param("b") Long userB);

    /** Latest message per unique conversation partner for inbox view */
    @Query("""
        SELECT m FROM Message m
        WHERE m.id IN (
            SELECT MAX(m2.id) FROM Message m2
            WHERE m2.sender.id = :uid OR m2.receiver.id = :uid
            GROUP BY CASE WHEN m2.sender.id = :uid THEN m2.receiver.id ELSE m2.sender.id END
        )
        ORDER BY m.sentAt DESC
    """)
    List<Message> findLatestPerConversation(@Param("uid") Long userId);

    /** Count unread messages sent TO this user */
    long countByReceiverIdAndReadAtIsNull(Long receiverId);

    /** Mark all messages in a conversation as read */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.readAt = :now WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.readAt IS NULL")
    void markConversationRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("now") Instant now);
}
