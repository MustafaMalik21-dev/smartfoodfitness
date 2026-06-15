package com.mustafa.smartfoodfitness.dto;

import java.util.List;
import java.util.Map;

public class ChatRequest {
    private Long userId;
    private List<Map<String, String>> messages; // [{ "role": "user"|"assistant", "content": "..." }]

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<Map<String, String>> getMessages() { return messages; }
    public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
}
