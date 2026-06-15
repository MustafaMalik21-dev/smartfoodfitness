package com.mustafa.smartfoodfitness.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustafa.smartfoodfitness.dto.ChatResponse;
import com.mustafa.smartfoodfitness.dto.FoodItemEstimate;
import com.mustafa.smartfoodfitness.dto.WeeklyInsightResponse;
import com.mustafa.smartfoodfitness.entity.FoodEntryLogs;
import com.mustafa.smartfoodfitness.entity.UserGoals;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WeightEntry;
import com.mustafa.smartfoodfitness.entity.WorkoutLog;
import com.mustafa.smartfoodfitness.repository.FoodEntryLogsRepository;
import com.mustafa.smartfoodfitness.repository.UserGoalsRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WeightEntryRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class AiService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FoodEntryLogsRepository foodRepo;
    private final WorkoutLogRepository workoutRepo;
    private final UserProfileRepository profileRepo;
    private final UserGoalsRepository goalsRepo;
    private final WeightEntryRepository weightRepo;

    public AiService(FoodEntryLogsRepository foodRepo, WorkoutLogRepository workoutRepo,
                     UserProfileRepository profileRepo, UserGoalsRepository goalsRepo,
                     WeightEntryRepository weightRepo) {
        this.foodRepo = foodRepo;
        this.workoutRepo = workoutRepo;
        this.profileRepo = profileRepo;
        this.goalsRepo = goalsRepo;
        this.weightRepo = weightRepo;
    }

    public List<FoodItemEstimate> analyzeFood(String base64Image, String mediaType) {
        try {
            String safeMediaType = (mediaType != null && !mediaType.isBlank()) ? mediaType : "image/jpeg";

            Map<String, Object> imageSource = new LinkedHashMap<>();
            imageSource.put("type", "base64");
            imageSource.put("media_type", safeMediaType);
            imageSource.put("data", base64Image);

            Map<String, Object> imageContent = new LinkedHashMap<>();
            imageContent.put("type", "image");
            imageContent.put("source", imageSource);

            Map<String, String> textContent = new LinkedHashMap<>();
            textContent.put("type", "text");
            textContent.put("text",
                "Analyze this food image. Identify each distinct food item visible. " +
                "Return ONLY a valid JSON array with no other text, using exactly this format: " +
                "[{\"name\":\"food name\",\"estimatedGrams\":150,\"calories\":250,\"protein\":30,\"carbs\":10,\"fat\":8}]. " +
                "Round all values to integers. Be conservative with portion estimates. " +
                "If no food is visible return [].");

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", List.of(imageContent, textContent));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 1024);
            body.put("messages", List.of(message));

            String rawText = callApi(body);

            // Strip markdown fences if Claude wraps the JSON
            String cleaned = rawText.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").trim();

            FoodItemEstimate[] items = objectMapper.readValue(cleaned, FoodItemEstimate[].class);
            return Arrays.asList(items);

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public WeeklyInsightResponse getWeeklyInsight(Long userProfileId) {
        try {
            Instant now = Instant.now();
            Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);

            // ── Food logs: group by day so averages are per-day, not per-entry ──
            List<FoodEntryLogs> recentFood = foodRepo
                .findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtDesc(userProfileId, sevenDaysAgo, now);

            // Group entries by truncated-day Instant, summing calories/protein.
            // Uses summingDouble so it handles Integer or Double fields safely.
            Map<Instant, Double> dailyCalMap = recentFood.stream()
                .filter(f -> f.getLoggedAt() != null)
                .collect(Collectors.groupingBy(
                    f -> f.getLoggedAt().truncatedTo(ChronoUnit.DAYS),
                    Collectors.summingDouble(f -> f.getCalories()  != null ? f.getCalories()  : 0)
                ));
            Map<Instant, Double> dailyProtMap = recentFood.stream()
                .filter(f -> f.getLoggedAt() != null)
                .collect(Collectors.groupingBy(
                    f -> f.getLoggedAt().truncatedTo(ChronoUnit.DAYS),
                    Collectors.summingDouble(f -> f.getProteins() != null ? f.getProteins() : 0)
                ));

            int foodLogDays = dailyCalMap.size();
            double avgDailyCalories = dailyCalMap.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
            double avgDailyProtein = dailyProtMap.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

            // Calorie adherence % across logged days
            int calorieAdherencePct = 0;

            // ── Workout logs ──────────────────────────────────────────────────
            List<WorkoutLog> recentWorkouts = workoutRepo
                .findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(userProfileId, sevenDaysAgo, now);
            int workoutCount = recentWorkouts.size();
            double totalMins = recentWorkouts.stream()
                .mapToDouble(w -> w.getDurationMinutes() != null ? w.getDurationMinutes() : 0)
                .sum();

            // ── Profile & goals ───────────────────────────────────────────────
            Optional<UserProfile> profileOpt = profileRepo.findById(userProfileId);
            Set<String> aims = profileOpt.map(UserProfile::getAims).orElse(Collections.emptySet());
            String aimsStr = aims.isEmpty() ? "general fitness" : String.join(", ", aims);

            Optional<UserGoals> goalsOpt = goalsRepo.findByUserProfileId(userProfileId);
            int calorieGoal = goalsOpt.map(g -> g.getCalorieGoal() != null ? g.getCalorieGoal() : 2000).orElse(2000);
            int proteinGoal = goalsOpt.map(g -> g.getProteinGoal() != null ? g.getProteinGoal() : 150).orElse(150);

            if (calorieGoal > 0 && avgDailyCalories > 0) {
                calorieAdherencePct = (int) Math.round(avgDailyCalories / calorieGoal * 100);
            }

            // ── Prompt ────────────────────────────────────────────────────────
            String prompt = String.format(
                "Weekly fitness data (last 7 days):\n" +
                "- Food logged: %d/7 days\n" +
                "- Avg daily calories: %.0f / %d goal (%d%%)\n" +
                "- Avg daily protein: %.0fg / %dg goal\n" +
                "- Workouts: %d (%.0f min total)\n" +
                "- Goals: %s\n\n" +
                "Reply with EXACTLY 3 sections, separated by a blank line.\n" +
                "Each section is exactly 2 lines:\n" +
                "  Line 1: emoji + short header (max 6 words, no punctuation)\n" +
                "  Line 2: one specific supporting sentence (max 14 words)\n\n" +
                "Use each emoji exactly once, in this order:\n" +
                "✅ — what they did well this week\n" +
                "⚠️ — the single most important thing to improve\n" +
                "💡 — one concrete action for today or tomorrow\n\n" +
                "Rules: be specific to the numbers above. No intro. No sign-off. No bullet points.",
                foodLogDays,
                avgDailyCalories, calorieGoal, calorieAdherencePct,
                avgDailyProtein, proteinGoal,
                workoutCount, totalMins,
                aimsStr
            );

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 280);
            body.put("messages", List.of(message));

            String insight = callApi(body).trim();
            return new WeeklyInsightResponse(insight, now.toString());

        } catch (Exception e) {
            return new WeeklyInsightResponse(
                "✅ Consistency is your biggest asset\n" +
                "Every session and meal logged moves you forward.\n\n" +
                "⚠️ Daily food logging needs attention\n" +
                "Tracking every day gives you accurate data to act on.\n\n" +
                "💡 Plan tomorrow's meals tonight\n" +
                "Five minutes of planning prevents poor choices tomorrow.",
                Instant.now().toString()
            );
        }
    }

    public ChatResponse chat(Long userId, List<Map<String, String>> history) {
        try {
            // ── Profile ────────────────────────────────────────────────────
            Optional<UserProfile> profileOpt = profileRepo.findById(userId);
            UserProfile profile = profileOpt.orElse(null);

            String name           = profile != null && profile.getDisplayName()    != null ? profile.getDisplayName()   : "User";
            String activityLevel  = profile != null && profile.getActivityLevel() != null ? profile.getActivityLevel() : "moderate";
            String experienceLevel= profile != null && profile.getExperienceLevel()!= null? profile.getExperienceLevel(): "beginner";
            String gender         = profile != null && profile.getGender()        != null ? profile.getGender()        : "unknown";
            String heightVal      = profile != null && profile.getHeightValue()   != null ? String.valueOf(profile.getHeightValue()) : "unknown";
            String heightUnit     = profile != null && profile.getHeightUnit()    != null ? profile.getHeightUnit()    : "";
            String weightVal      = profile != null && profile.getWeightValue()   != null ? String.valueOf(profile.getWeightValue()) : "unknown";
            String weightUnit     = profile != null && profile.getWeightUnit()    != null ? profile.getWeightUnit()    : "kg";
            String age            = profile != null && profile.getAge()           != null ? String.valueOf(profile.getAge())         : "unknown";
            Set<String> aims      = profile != null ? profile.getAims() : Collections.emptySet();
            String aimsStr        = aims.isEmpty() ? "general fitness" : String.join(", ", aims);

            // ── Goals ──────────────────────────────────────────────────────
            Optional<UserGoals> goalsOpt = goalsRepo.findByUserProfileId(userId);
            int calorieGoal = goalsOpt.map(g -> g.getCalorieGoal() != null ? g.getCalorieGoal() : 2000).orElse(2000);
            int proteinGoal = goalsOpt.map(g -> g.getProteinGoal() != null ? g.getProteinGoal() : 150).orElse(150);

            // ── Latest body composition (most recent weight entry) ─────────
            Optional<WeightEntry> latestWeightOpt = weightRepo.findTopByUserProfileIdOrderByRecordedAtDesc(userId);
            WeightEntry lw = latestWeightOpt.orElse(null);
            String bmi             = lw != null && lw.getBmi()            != null ? String.format("%.1f", lw.getBmi())            : "not measured";
            String bodyFat         = lw != null && lw.getBodyFatPercent() != null ? String.format("%.1f%%", lw.getBodyFatPercent()): "not measured";
            String muscleMass      = lw != null && lw.getMuscleMassKg()   != null ? String.format("%.1f kg", lw.getMuscleMassKg()): "not measured";
            String waterPct        = lw != null && lw.getWaterPercent()   != null ? String.format("%.1f%%", lw.getWaterPercent())  : "not measured";
            String visceralFat     = lw != null && lw.getVisceralFatLevel()!= null? String.valueOf(lw.getVisceralFatLevel())       : "not measured";
            String bmr             = lw != null && lw.getBmr()            != null ? lw.getBmr() + " kcal"                         : "not measured";
            String proteinPct      = lw != null && lw.getProteinPercent() != null ? String.format("%.1f%%", lw.getProteinPercent()): "not measured";

            // ── Today's food ───────────────────────────────────────────────
            Instant now = Instant.now();
            Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
            List<FoodEntryLogs> todayFood = foodRepo
                .findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtDesc(userId, startOfDay, now);
            double todayCalories = todayFood.stream()
                .mapToDouble(f -> f.getCalories() != null ? f.getCalories() : 0).sum();
            double todayProtein  = todayFood.stream()
                .mapToDouble(f -> f.getProteins() != null ? f.getProteins() : 0).sum();
            double todayCarbs    = todayFood.stream()
                .mapToDouble(f -> f.getCarbs()    != null ? f.getCarbs()    : 0).sum();
            double todayFat      = todayFood.stream()
                .mapToDouble(f -> f.getFats()     != null ? f.getFats()     : 0).sum();
            // Recent food names (last 5 entries today)
            String todayFoodNames = todayFood.stream()
                .limit(5)
                .map(f -> f.getFoodName() != null ? f.getFoodName() : "unknown")
                .reduce((a, b) -> a + ", " + b)
                .orElse("nothing logged yet");

            // ── Workouts this week ─────────────────────────────────────────
            Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
            List<WorkoutLog> recentWorkouts = workoutRepo
                .findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(userId, weekAgo, now);
            int workoutCount = recentWorkouts.size();
            double totalWorkoutMins = recentWorkouts.stream()
                .mapToDouble(w -> w.getDurationMinutes() != null ? w.getDurationMinutes() : 0).sum();

            // ── System prompt with full context ────────────────────────────
            String systemPrompt = String.format(
                "You are an AI fitness and nutrition assistant built into the SmartFoodFitness app. " +
                "You have full access to the user's data below — use it to give specific, personalised answers. " +
                "Be concise and friendly. Keep replies under 150 words. Use plain text only — no markdown, no bullet symbols. " +
                "Never give medical advice; recommend a professional for health concerns.\n\n" +
                "=== USER PROFILE ===\n" +
                "Name: %s | Age: %s | Gender: %s\n" +
                "Height: %s %s | Current weight: %s %s\n" +
                "Activity level: %s | Experience: %s\n" +
                "Goals: %s\n\n" +
                "=== BODY COMPOSITION (latest scan) ===\n" +
                "BMI: %s | Body fat: %s | Muscle mass: %s\n" +
                "Body water: %s | Visceral fat level: %s\n" +
                "BMR: %s | Protein %%: %s\n\n" +
                "=== DAILY TARGETS ===\n" +
                "Calories: %d kcal | Protein: %dg\n\n" +
                "=== TODAY'S NUTRITION ===\n" +
                "Calories: %.0f / %d kcal | Protein: %.0fg | Carbs: %.0fg | Fat: %.0fg\n" +
                "Foods logged today: %s\n\n" +
                "=== FITNESS (last 7 days) ===\n" +
                "Workouts completed: %d (%.0f min total)",
                name, age, gender,
                heightVal, heightUnit, weightVal, weightUnit,
                activityLevel, experienceLevel,
                aimsStr,
                bmi, bodyFat, muscleMass,
                waterPct, visceralFat,
                bmr, proteinPct,
                calorieGoal, proteinGoal,
                todayCalories, calorieGoal, todayProtein, todayCarbs, todayFat,
                todayFoodNames,
                workoutCount, totalWorkoutMins
            );

            // Build API messages list from conversation history
            List<Map<String, Object>> apiMessages = new ArrayList<>();
            for (Map<String, String> msg : history) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("role", msg.get("role"));
                m.put("content", msg.get("content") != null ? msg.get("content") : "");
                apiMessages.add(m);
            }

            // Ensure messages alternate properly — API requires user first
            if (apiMessages.isEmpty() || !"user".equals(apiMessages.get(0).get("role"))) {
                return new ChatResponse("Please send a message to get started!");
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 350);
            body.put("system", systemPrompt);
            body.put("messages", apiMessages);

            String reply = callApi(body).trim();
            return new ChatResponse(reply);

        } catch (Exception e) {
            return new ChatResponse("Sorry, I'm having trouble connecting right now. Please try again in a moment.");
        }
    }

    private String callApi(Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(ANTHROPIC_URL, HttpMethod.POST, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("content").get(0).path("text").asText();
    }
}
