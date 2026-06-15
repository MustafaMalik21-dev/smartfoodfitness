package com.mustafa.smartfoodfitness.controller;

import com.mustafa.smartfoodfitness.dto.AnalyzeFoodRequest;
import com.mustafa.smartfoodfitness.dto.ChatRequest;
import com.mustafa.smartfoodfitness.dto.ChatResponse;
import com.mustafa.smartfoodfitness.dto.FoodItemEstimate;
import com.mustafa.smartfoodfitness.dto.WeeklyInsightResponse;
import com.mustafa.smartfoodfitness.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze-food")
    public ResponseEntity<List<FoodItemEstimate>> analyzeFood(@RequestBody AnalyzeFoodRequest request) {
        List<FoodItemEstimate> items = aiService.analyzeFood(request.getImageBase64(), request.getMediaType());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/weekly-insight/{userProfileId}")
    public ResponseEntity<WeeklyInsightResponse> weeklyInsight(@PathVariable Long userProfileId) {
        WeeklyInsightResponse response = aiService.getWeeklyInsight(userProfileId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request.getUserId(), request.getMessages());
        return ResponseEntity.ok(response);
    }
}
