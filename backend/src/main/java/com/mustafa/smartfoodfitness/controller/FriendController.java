package com.mustafa.smartfoodfitness.controller;

import com.mustafa.smartfoodfitness.dto.FriendDto;
import com.mustafa.smartfoodfitness.dto.UserSearchResultDto;
import com.mustafa.smartfoodfitness.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /** GET /api/friends/search?userId=1&q=john */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDto>> search(
            @RequestParam Long userId,
            @RequestParam String q) {
        return ResponseEntity.ok(friendService.searchUsers(userId, q));
    }

    /** GET /api/friends/user/{userId} — accepted friends */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FriendDto>> getFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    /** GET /api/friends/requests/user/{userId} — pending in/out */
    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<FriendDto>> getPending(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }

    /** POST /api/friends/request  body: { senderId, receiverId } */
    @PostMapping("/request")
    public ResponseEntity<FriendDto> sendRequest(@RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(friendService.sendRequest(body.get("senderId"), body.get("receiverId")));
    }

    /** PUT /api/friends/{requestId}/accept?userId=2 */
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<FriendDto> accept(@PathVariable Long requestId, @RequestParam Long userId) {
        return ResponseEntity.ok(friendService.respondToRequest(requestId, userId, true));
    }

    /** PUT /api/friends/{requestId}/decline?userId=2 */
    @PutMapping("/{requestId}/decline")
    public ResponseEntity<FriendDto> decline(@PathVariable Long requestId, @RequestParam Long userId) {
        return ResponseEntity.ok(friendService.respondToRequest(requestId, userId, false));
    }

    /** DELETE /api/friends/{requestId}?userId=1 */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> remove(@PathVariable Long requestId, @RequestParam Long userId) {
        friendService.removeFriend(requestId, userId);
        return ResponseEntity.noContent().build();
    }
}
