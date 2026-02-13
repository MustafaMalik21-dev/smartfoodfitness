package com.mustafa.smartfoodfitness.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MealDbService {

    private static final String BASE = "https://www.themealdb.com/api/json/v1/1";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RecipeSummary> search(String query) {
        String url = BASE + "/search.php?s=" + enc(query);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public RecipeDetail lookup(String mealId) {
        String url = BASE + "/lookup.php?i=" + enc(mealId);
        MealDbRawResponse raw = get(url);
        if (raw == null || raw.meals == null || raw.meals.isEmpty()) return null;
        return toDetail(raw.meals.get(0));
    }

    public List<RecipeSummary> random() {
        String url = BASE + "/random.php";
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public List<RecipeSummary> byCategory(String category) {
        String url = BASE + "/filter.php?c=" + enc(category);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public List<RecipeSummary> byIngredient(String ingredient) {
        String url = BASE + "/filter.php?i=" + enc(ingredient);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    private MealDbRawResponse get(String url) {
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
        String body = res.getBody();
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readValue(body, MealDbRawResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<RecipeSummary> toSummaries(MealDbRawResponse raw) {
        if (raw == null || raw.meals == null) return List.of();
        List<RecipeSummary> out = new ArrayList<>();
        for (Map<String, Object> m : raw.meals) {
            RecipeSummary s = new RecipeSummary();
            s.setMealId(str(m.get("idMeal")));
            s.setName(str(m.get("strMeal")));
            s.setThumbUrl(str(m.get("strMealThumb")));
            out.add(s);
        }
        return out;
    }

    private RecipeDetail toDetail(Map<String, Object> m) {
        RecipeDetail d = new RecipeDetail();
        d.setMealId(str(m.get("idMeal")));
        d.setName(str(m.get("strMeal")));
        d.setCategory(str(m.get("strCategory")));
        d.setArea(str(m.get("strArea")));
        d.setInstructions(str(m.get("strInstructions")));
        d.setThumbUrl(str(m.get("strMealThumb")));
        d.setTags(str(m.get("strTags")));
        d.setYoutubeUrl(str(m.get("strYoutube")));
        d.setSourceUrl(str(m.get("strSource")));

        List<IngredientLine> lines = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String ing = str(m.get("strIngredient" + i));
            String meas = str(m.get("strMeasure" + i));
            if (ing != null && !ing.isBlank()) {
                IngredientLine line = new IngredientLine();
                line.setIngredient(ing.trim());
                line.setMeasure(meas == null ? "" : meas.trim());
                lines.add(line);
            }
        }
        d.setIngredients(lines);
        return d;
    }

    private String enc(String s) {
        if (s == null) return "";
        return s.replace(" ", "%20");
    }

    private String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }

    // ===== DTOs (kept here for simplicity) =====

    public static class RecipeSummary {
        private String mealId;
        private String name;
        private String thumbUrl;

        public String getMealId() { return mealId; }
        public void setMealId(String mealId) { this.mealId = mealId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getThumbUrl() { return thumbUrl; }
        public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    }

    public static class RecipeDetail {
        private String mealId;
        private String name;
        private String category;
        private String area;
        private String instructions;
        private String thumbUrl;
        private String tags;
        private String youtubeUrl;
        private String sourceUrl;
        private List<IngredientLine> ingredients = new ArrayList<>();

        public String getMealId() { return mealId; }
        public void setMealId(String mealId) { this.mealId = mealId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }

        public String getThumbUrl() { return thumbUrl; }
        public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public String getYoutubeUrl() { return youtubeUrl; }
        public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

        public String getSourceUrl() { return sourceUrl; }
        public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

        public List<IngredientLine> getIngredients() { return ingredients; }
        public void setIngredients(List<IngredientLine> ingredients) { this.ingredients = ingredients; }
    }

    public static class IngredientLine {
        private String ingredient;
        private String measure;

        public String getIngredient() { return ingredient; }
        public void setIngredient(String ingredient) { this.ingredient = ingredient; }

        public String getMeasure() { return measure; }
        public void setMeasure(String measure) { this.measure = measure; }
    }

    public static class MealDbRawResponse {
        public List<Map<String, Object>> meals = new ArrayList<>();
    }
}
