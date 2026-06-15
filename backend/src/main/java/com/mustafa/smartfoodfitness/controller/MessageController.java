package com.mustafa.smartfoodfitness.controller;

import com.mustafa.smartfoodfitness.dto.MessageDto;
import com.mustafa.smartfoodfitness.dto.SendMessageRequest;
import com.mustafa.smartfoodfitness.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** POST /api/messages  body: { senderId, receiverId, content } */
    @PostMapping
    public ResponseEntity<MessageDto> send(@RequestBody SendMessageRequest req) {
        return ResponseEntity.ok(messageService.send(req.getSenderId(), req.getReceiverId(), req.getContent()));
    }

    /** GET /api/messages/conversation?userA=1&userB=2 */
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDto>> conversation(
            @RequestParam Long userA,
            @RequestParam Long userB) {
        return ResponseEntity.ok(messageService.getConversation(userA, userB));
    }

    /** GET /api/messages/inbox/{userId} — latest message per conversation */
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<MessageDto>> inbox(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getInbox(userId));
    }

    /** GET /api/messages/unread/{userId} — count of unread messages */
    @GetMapping("/unread/{userId}")
    public ResponseEntity<Map<String, Long>> unread(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(userId)));
    }
}
