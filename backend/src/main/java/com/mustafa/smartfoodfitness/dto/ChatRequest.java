package com.mustafa.smartfoodfitness.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatRequest {
    @NotNull(message = "userId is required.")
    private Long userId;

    // Outer sanity bound only — tighter per-request history limits belong in AiService.
    @Size(max = 500, message = "Conversation history is too long.")
    private List<Map<String, String>> messages; // [{ "role": "user"|"assistant", "content": "..." }]

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<Map<String, String>> getMessages() { return messages; }
    public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
}
