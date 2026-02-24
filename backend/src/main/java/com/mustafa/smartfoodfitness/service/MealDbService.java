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
public class MealDbService { // Define the MealDbService class responsible for interacting with the MealDB API to perform operations such as searching for recipes, looking up recipe details, retrieving random recipes, and filtering recipes by category or ingredient, with methods to handle API requests and responses, and inner classes to represent recipe summaries, details, ingredient lines, and raw API responses for structured data handling within the application

    private static final String BASE = "https://www.themealdb.com/api/json/v1/1";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RecipeSummary> search(String query) { // handle HTTP GET requests to search for recipes in the MealDb API based on user queries, accepting a query parameter for the search term, and returning a list of response DTOs representing the matching recipes to the client when they access the relevant endpoint in the application
        String url = BASE + "/search.php?s=" + enc(query);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public RecipeDetail lookup(String mealId) { // handle HTTP GET requests to retrieve detailed information about a specific recipe from the MealDb API, accepting a path variable representing the meal ID, and returning a response DTO containing the recipe details to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the recipe does not exist
        String url = BASE + "/lookup.php?i=" + enc(mealId);
        MealDbRawResponse raw = get(url);
        if (raw == null || raw.meals == null || raw.meals.isEmpty()) return null;
        return toDetail(raw.meals.get(0));
    }

    public List<RecipeSummary> random() { // handle HTTP GET requests to retrieve random recipes from the MealDb API, and returning a list of response DTOs representing the random recipes to the client when they access the relevant endpoint in the application
        String url = BASE + "/random.php";
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public List<RecipeSummary> byCategory(String category) { // handle HTTP GET requests to retrieve recipes from the MealDb API based on a specified category, accepting a path variable representing the category name, and returning a list of response DTOs representing the recipes in that category to the client when they access the relevant endpoint in the application
        String url = BASE + "/filter.php?c=" + enc(category);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    public List<RecipeSummary> byIngredient(String ingredient) { // handle HTTP GET requests to retrieve recipes from the MealDb API based on a specified ingredient, accepting a path variable representing the ingredient name, and returning a list of response DTOs representing the recipes that include that ingredient to the client when they access the relevant endpoint in the application
        String url = BASE + "/filter.php?i=" + enc(ingredient);
        MealDbRawResponse raw = get(url);
        return toSummaries(raw);
    }

    private MealDbRawResponse get(String url) { // Helper method to perform HTTP GET requests to the MealDb API, accepting a URL as a parameter, and returning a deserialized response object containing the raw API response data for further processing within the service methods
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
        String body = res.getBody();
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readValue(body, MealDbRawResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<RecipeSummary> toSummaries(MealDbRawResponse raw) { // Helper method to convert raw API response data into a list of RecipeSummary DTOs, accepting a MealDbRawResponse object as a parameter, and returning a list of structured recipe summary information for use in the service methods and ultimately for client responses when they access the relevant endpoints in the application
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

    private RecipeDetail toDetail(Map<String, Object> m) { // Helper method to convert raw API response data for a specific recipe into a RecipeDetail DTO, accepting a map representing the raw recipe data as a parameter, and returning a structured recipe detail object containing comprehensive information about the recipe for use in the service methods and ultimately for client responses when they access the relevant endpoints in the application
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

    private String enc(String s) { // Helper method to URL-encode a string for use in API request parameters, accepting a string as a parameter, and returning an encoded version of the string with spaces replaced by "%20" for proper formatting in API requests when they are made to the MealDb API endpoints
        if (s == null) return "";
        return s.replace(" ", "%20");
    }

    private String str(Object o) { // Helper method to safely convert an object to a string, accepting an object as a parameter, and returning a string representation of the object while handling null values and treating the string "null" (case-insensitive) as a null value for consistent data processing within the service methods when they access raw API response data
        if (o == null) return null;
        String s = String.valueOf(o);
        return "null".equalsIgnoreCase(s) ? null : s;
    }

    public static class RecipeSummary { // Define the RecipeSummary DTO representing a summary of recipe information, with fields for meal ID, name, and thumbnail URL, along with getter and setter methods for each field to facilitate data transfer of recipe summary information between the backend and frontend of the application when users access the relevant endpoints for recipe search and retrieval
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

    public static class RecipeDetail { // Define the RecipeDetail DTO representing detailed information about a recipe, with fields for meal ID, name, category, area, instructions, thumbnail URL, tags, YouTube URL, source URL, and a list of ingredient lines, along with getter and setter methods for each field to facilitate data transfer of comprehensive recipe information between the backend and frontend of the application when users access the relevant endpoints for recipe lookup and retrieval
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

        public String getMealId() { 
            return mealId; 
        }
        public void setMealId(String mealId) { 
            this.mealId = mealId; 
        }

        public String getName() { 
            return name; 
        }
        public void setName(String name) { 
            this.name = name; 
        }

        public String getCategory() { 
            return category; 
        }
        public void setCategory(String category) { 
            this.category = category; 
        }

        public String getArea() { 
            return area; 
        }
        public void setArea(String area) { 
            this.area = area; 
        }

        public String getInstructions() { 
            return instructions; 
        }
        public void setInstructions(String instructions) { 
            this.instructions = instructions; 
        }

        public String getThumbUrl() { 
            return thumbUrl; 
        }
        public void setThumbUrl(String thumbUrl) { 
            this.thumbUrl = thumbUrl; 
        }

        public String getTags() { 
            return tags; 
        }
        public void setTags(String tags) { 
            this.tags = tags; 
        }

        public String getYoutubeUrl() { 
            return youtubeUrl; 
        }
        public void setYoutubeUrl(String youtubeUrl) { 
            this.youtubeUrl = youtubeUrl; 
        }

        public String getSourceUrl() { 
            return sourceUrl; 
        }
        public void setSourceUrl(String sourceUrl) { 
            this.sourceUrl = sourceUrl; 
        }

        public List<IngredientLine> getIngredients() { 
            return ingredients; 
        }
        public void setIngredients(List<IngredientLine> ingredients) { 
            this.ingredients = ingredients; 
        }
    }

    public static class IngredientLine { // Define the IngredientLine DTO representing a single ingredient line in a recipe, with fields for the ingredient name and its measure, along with getter and setter methods for each field to facilitate structured representation of recipe ingredients within the RecipeDetail DTO when users access detailed recipe information through the relevant endpoints in the application
        private String ingredient;
        private String measure;

        public String getIngredient() { 
            return ingredient; 
        }
        public void setIngredient(String ingredient) { 
            this.ingredient = ingredient; 
        }

        public String getMeasure() { 
            return measure; 
        }
        public void setMeasure(String measure) { 
            this.measure = measure; 
        }
    }

    public static class MealDbRawResponse { // Define the MealDbRawResponse DTO representing the raw response structure from the MealDb API, with a field for a list of meals represented as maps of string keys to object values, along with getter and setter methods for the meals field to facilitate deserialization of API responses into a structured format for further processing within the service methods when they access the MealDb API endpoints
        public List<Map<String, Object>> meals = new ArrayList<>();
    }
}
