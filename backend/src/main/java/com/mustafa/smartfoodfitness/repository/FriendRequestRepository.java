package com.mustafa.smartfoodfitness.repository;

import com.mustafa.smartfoodfitness.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /** All accepted requests involving this user (either side) */
    @Query("SELECT f FROM FriendRequest f WHERE f.status = 'ACCEPTED' AND (f.sender.id = :uid OR f.receiver.id = :uid)")
    List<FriendRequest> findFriends(@Param("uid") Long userId);

    /** Incoming pending requests for this user */
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, String status);

    /** Outgoing pending requests sent by this user */
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, String status);

    /** Check if a request already exists between two users (either direction) */
    @Query("SELECT f FROM FriendRequest f WHERE (f.sender.id = :a AND f.receiver.id = :b) OR (f.sender.id = :b AND f.receiver.id = :a)")
    Optional<FriendRequest> findBetween(@Param("a") Long userA, @Param("b") Long userB);

    // Both directions, or the surviving side keeps a dangling FK to the deleted user.
    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM FriendRequest f WHERE f.sender.id = :uid OR f.receiver.id = :uid")
    void deleteAllForUser(@Param("uid") Long userId);
}
