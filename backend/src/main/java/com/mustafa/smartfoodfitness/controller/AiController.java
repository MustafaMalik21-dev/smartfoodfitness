package com.mustafa.smartfoodfitness.controller;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.AnalyzeFoodRequest;
import com.mustafa.smartfoodfitness.dto.ChatRequest;
import com.mustafa.smartfoodfitness.dto.ChatResponse;
import com.mustafa.smartfoodfitness.dto.FoodItemEstimate;
import com.mustafa.smartfoodfitness.dto.GeneratePlanRequest;
import com.mustafa.smartfoodfitness.dto.GeneratePlanResponse;
import com.mustafa.smartfoodfitness.dto.WeeklyInsightResponse;
import com.mustafa.smartfoodfitness.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    // ~10 MB of base64 ≈ 7.5 MB image — generous for a phone photo, blocks abuse
    private static final int MAX_IMAGE_BASE64_CHARS = 10_000_000;
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze-food")
    public ResponseEntity<List<FoodItemEstimate>> analyzeFood(@RequestBody AnalyzeFoodRequest request) {
        String image = request.getImageBase64();
        if (image == null || image.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageBase64 is required.");
        }
        if (image.length() > MAX_IMAGE_BASE64_CHARS) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image too large.");
        }
        String mediaType = normaliseMediaType(request.getMediaType());
        List<FoodItemEstimate> items = aiService.analyzeFood(image, mediaType);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/weekly-insight/{userProfileId}")
    public ResponseEntity<WeeklyInsightResponse> weeklyInsight(@PathVariable Long userProfileId) {
        AuthGuard.requireSelf(userProfileId);
        WeeklyInsightResponse response = aiService.getWeeklyInsight(userProfileId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        AuthGuard.requireSelf(request.getUserId());
        ChatResponse response = aiService.chat(request.getUserId(), request.getMessages());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<GeneratePlanResponse> generatePlan(@Valid @RequestBody GeneratePlanRequest request) {
        return ResponseEntity.ok(aiService.generatePlan(request));
    }

    /** Unknown or aliased types fall back to JPEG rather than rejecting the photo. */
    private static String normaliseMediaType(String mediaType) {
        if (mediaType == null || mediaType.isBlank()) return "image/jpeg";
        String t = mediaType.trim().toLowerCase();
        if ("image/jpg".equals(t)) return "image/jpeg";
        return ALLOWED_IMAGE_TYPES.contains(t) ? t : "image/jpeg";
    }
}
