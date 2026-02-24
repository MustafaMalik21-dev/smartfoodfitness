package com.mustafa.smartfoodfitness.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.mustafa.smartfoodfitness.dto.WgerEncyclopediaResponse;
import com.mustafa.smartfoodfitness.dto.WgerExerciseDetailResponse;
import com.mustafa.smartfoodfitness.dto.WgerExerciseTile;

@Service
public class WgerExerciseService {

    private final RestClient wger;
    private final int languageId;

    private volatile Map<Integer, String> categoryIdToName = new HashMap<>();
    private volatile Instant categoryCacheAt = Instant.EPOCH;

    private volatile Map<Integer, String> muscleIdToName = new HashMap<>();
    private volatile Instant muscleCacheAt = Instant.EPOCH;

    public WgerExerciseService(RestClient wgerRestClient, @Value("${wger.language-id}") int languageId) { // Constructor for the WgerExerciseService class, accepting a RestClient for making API calls to the wger exercise database and a language ID for handling translations, initializing the service with these dependencies to facilitate fetching exercise information, categories, and muscles from the wger API when users access the relevant endpoints in the application
        this.wger = wgerRestClient;
        this.languageId = languageId;
    }

    public WgerEncyclopediaResponse getEncyclopedia(int perCategoryLimit) { // Method to retrieve an encyclopedia of exercises from the wger API, accepting a parameter to specify the number of exercises to return per category, ensuring that the category and muscle caches are populated, fetching exercise information from the API, categorizing exercises based on their mapped categories, and returning a WgerEncyclopediaResponse DTO containing lists of WgerExerciseTile DTOs for each exercise category to be sent back to the client when they access the relevant endpoint in the application
        int limit = Math.max(1, Math.min(perCategoryLimit, 30));

        ensureCategoryCache();
        ensureMuscleCache();

        List<Map<String, Object>> all = fetchExerciseInfoPages(700);

        List<WgerExerciseTile> chest = new ArrayList<>();
        List<WgerExerciseTile> back = new ArrayList<>();
        List<WgerExerciseTile> shoulders = new ArrayList<>();
        List<WgerExerciseTile> arms = new ArrayList<>();
        List<WgerExerciseTile> core = new ArrayList<>();
        List<WgerExerciseTile> legs = new ArrayList<>();
        List<WgerExerciseTile> cardio = new ArrayList<>();

        for (Map<String, Object> ex : all) { // Iterate through the fetched exercise information, extracting relevant details such as exercise ID, name, category, and muscle involvement, mapping exercises to predefined categories based on their attributes, and populating lists of WgerExerciseTile DTOs for each category while respecting the specified limit for exercises per category to be included in the WgerEncyclopediaResponse DTO that will be returned to the client when they access the relevant endpoint in the application
            Integer id = asInt(ex.get("id"));
            String name = pickTranslatedField(ex, "name");
            if (id == null || name == null || name.isBlank()) continue;

            if (looksNonLatin(name)) continue;

            String mapped = mapToYourCategory(ex);
            if (mapped == null) continue;

            WgerExerciseTile tile = new WgerExerciseTile();
            tile.setId(id);
            tile.setName(name);
            tile.setCategory(mapped);
            tile.setImageUrl(firstImageUrl(ex));

            if ("Chest".equals(mapped) && chest.size() < limit) chest.add(tile);
            else if ("Back".equals(mapped) && back.size() < limit) back.add(tile);
            else if ("Shoulders".equals(mapped) && shoulders.size() < limit) shoulders.add(tile);
            else if ("Arms".equals(mapped) && arms.size() < limit) arms.add(tile);
            else if ("Core".equals(mapped) && core.size() < limit) core.add(tile);
            else if ("Legs".equals(mapped) && legs.size() < limit) legs.add(tile);
            else if ("Cardio".equals(mapped) && cardio.size() < limit) cardio.add(tile);
        }

        WgerEncyclopediaResponse resp = new WgerEncyclopediaResponse();
        resp.setChest(chest);
        resp.setBack(back);
        resp.setShoulders(shoulders);
        resp.setArms(arms);
        resp.setCore(core);
        resp.setLegs(legs);
        resp.setCardio(cardio);
        return resp;
    }

    public List<WgerExerciseTile> search(String q, int limit, String scope) { // Method to search for exercises in the wger API based on user queries, accepting parameters for the search term, result limit, and search scope, ensuring that the category and muscle caches are populated, fetching exercise information from the API, filtering exercises based on the search query and specified scope (upper body, lower body, or all), mapping exercises to predefined categories, and returning a list of WgerExerciseTile DTOs representing the matching exercises to be sent back to the client when they access the relevant endpoint in the application
        ensureCategoryCache();
        ensureMuscleCache();

        int lim = Math.max(1, Math.min(limit, 200));
        String query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        String sc = (scope == null) ? "all" : scope.trim().toLowerCase(Locale.ROOT);

        Set<String> allowed = new HashSet<>();
        if ("upper".equals(sc)) {
            allowed.add("Chest");
            allowed.add("Back");
            allowed.add("Shoulders");
            allowed.add("Arms");
        } else if ("lower".equals(sc)) {
            allowed.add("Core");
            allowed.add("Legs");
            allowed.add("Cardio");
        }

        List<Map<String, Object>> all = fetchExerciseInfoPages(900);
        List<WgerExerciseTile> out = new ArrayList<>();

        for (Map<String, Object> ex : all) {
            Integer id = asInt(ex.get("id"));
            String name = asString(ex.get("name"));
            if (id == null || name == null || name.isBlank()) continue;

            if (looksNonLatin(name)) continue;

            String mapped = mapToYourCategory(ex);
            if (mapped == null) continue;

            if (!allowed.isEmpty() && !allowed.contains(mapped)) continue;

            String hay = (name + " " + mapped + " " + String.join(" ", extractMuscleNames(ex))).toLowerCase(Locale.ROOT);
            if (!query.isBlank() && !hay.contains(query)) continue;

            WgerExerciseTile tile = new WgerExerciseTile();
            tile.setId(id);
            tile.setName(name);
            tile.setCategory(mapped);
            tile.setImageUrl(firstImageUrl(ex));
            out.add(tile);

            if (out.size() >= lim) break;
        }

        return out;
    }

    public WgerExerciseDetailResponse getExerciseDetail(int id) { // Method to retrieve detailed information about a specific exercise from the wger API, accepting an exercise ID as a parameter, ensuring that the category and muscle caches are populated, fetching the exercise information from the API based on the provided ID, extracting relevant details such as exercise name, description, images, involved muscles, and equipment, mapping the exercise to a predefined category, and returning a WgerExerciseDetailResponse DTO containing the exercise details to be sent back to the client when they access the relevant endpoint in the application
        Map<String, Object> ex = wger.get()
                .uri(uri -> uri.path("/exerciseinfo/" + id + "/").queryParam("language", languageId).build())
                .retrieve()
                .body(Map.class);

        if (ex == null) ex = new HashMap<>();

        WgerExerciseDetailResponse r = new WgerExerciseDetailResponse();
        r.setId(asInt(ex.get("id")));
        r.setName(pickTranslatedField(ex, "name"));
        r.setDescription(pickTranslatedField(ex, "description"));
        r.setImages(extractImageUrls(ex));
        r.setMuscles(extractMuscleNames(ex));
        r.setEquipment(extractNamedList(ex, "equipment"));
        return r;
    }


    private String mapToYourCategory(Map<String, Object> ex) { // Helper method to map exercises from the wger API to predefined categories (Chest, Back, Shoulders, Arms, Core, Legs, Cardio) based on exercise attributes such as name, category, and involved muscles, accepting a map representing the exercise information, extracting relevant details, and applying heuristic rules to determine the appropriate category for the exercise to facilitate categorization within the application when users access exercise information through the relevant endpoints
        String name = asString(ex.get("name"));
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);

        String catName = extractCategoryName(ex);
        String c = catName == null ? "" : catName.toLowerCase(Locale.ROOT);

        List<String> muscles = extractMuscleNames(ex);
        String m = String.join(" ", muscles).toLowerCase(Locale.ROOT);

        if (c.contains("cardio") || c.contains("endurance") || n.contains("run") || n.contains("bike") || n.contains("burpee")) {
            return "Cardio";
        }

        // Chest
        if (c.contains("chest") || n.contains("bench") || n.contains("pec") || m.contains("pector")) return "Chest";

        // Back
        if (c.contains("back") || n.contains("row") || n.contains("pull") || m.contains("latissimus") || m.contains("trapezius") || m.contains("rhombo")) return "Back";

        // Shoulders
        if (c.contains("shoulder") || n.contains("overhead") || m.contains("deltoid")) return "Shoulders";

        // Arms
        if (c.contains("arm") || n.contains("curl") || n.contains("tricep") || m.contains("biceps") || m.contains("triceps") || m.contains("forearm")) return "Arms";

        // Core
        if (c.contains("abs") || c.contains("core") || n.contains("plank") || n.contains("crunch") || m.contains("abdom") || m.contains("oblique")) return "Core";

        // Legs
        if (c.contains("leg") || n.contains("squat") || n.contains("lunge") || n.contains("deadlift")
                || m.contains("quadriceps") || m.contains("hamstring") || m.contains("glute") || m.contains("calf")) return "Legs";

        // Final fallback based on category name only
        if (c.contains("abs")) return "Core";
        if (c.contains("legs") || c.contains("calves")) return "Legs";
        if (c.contains("arms")) return "Arms";
        if (c.contains("shoulders")) return "Shoulders";
        if (c.contains("back")) return "Back";
        if (c.contains("chest")) return "Chest";

        return null;
    }


    private void ensureCategoryCache() { // Helper method to ensure that the category cache is populated with up-to-date information from the wger API, checking if the cache is still valid based on a defined time threshold, and if not, fetching the category information from the API, processing it to extract category IDs and names, and updating the cache for use in mapping exercises to categories within the application when users access exercise information through the relevant endpoints
        Instant now = Instant.now();
        if (!categoryIdToName.isEmpty() && Duration.between(categoryCacheAt, now).toMinutes() < 120) return;

        Map<Integer, String> map = new HashMap<>();
        String next = "/exercisecategory/?limit=200";

        while (next != null) {
            Map<String, Object> page = wger.get().uri(next).retrieve().body(Map.class);
            if (page == null) break;

            Object resultsObj = page.get("results");
            if (resultsObj instanceof List) {
                for (Object o : (List<?>) resultsObj) {
                    if (o instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) o;
                        Integer id = asInt(m.get("id"));
                        String name = asString(m.get("name"));
                        if (id != null && name != null && !name.isBlank()) map.put(id, name);
                    }
                }
            }

            Object nextObj = page.get("next");
            next = (nextObj instanceof String) ? stripBase((String) nextObj) : null;
        }

        categoryIdToName = map;
        categoryCacheAt = now;
    }

    private void ensureMuscleCache() { // Helper method to ensure that the muscle cache is populated with up-to-date information from the wger API, checking if the cache is still valid based on a defined time threshold, and if not, fetching the muscle information from the API, processing it to extract muscle IDs and names, and updating the cache for use in mapping exercises to involved muscles within the application when users access exercise information through the relevant endpoints
        Instant now = Instant.now();
        if (!muscleIdToName.isEmpty() && Duration.between(muscleCacheAt, now).toMinutes() < 120) return;

        Map<Integer, String> map = new HashMap<>();
        String next = "/muscle/?limit=200";

        while (next != null) {
            Map<String, Object> page = wger.get().uri(next).retrieve().body(Map.class);
            if (page == null) break;

            Object resultsObj = page.get("results");
            if (resultsObj instanceof List) {
                for (Object o : (List<?>) resultsObj) {
                    if (o instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) o;
                        Integer id = asInt(m.get("id"));
                        String name = asString(m.get("name"));
                        if (id != null && name != null && !name.isBlank()) map.put(id, name);
                    }
                }
            }

            Object nextObj = page.get("next");
            next = (nextObj instanceof String) ? stripBase((String) nextObj) : null;
        }

        muscleIdToName = map;
        muscleCacheAt = now;
    }

    private String extractCategoryName(Map<String, Object> ex) { // Helper method to extract the category name for an exercise from the raw exercise information returned by the wger API, accepting a map representing the exercise information, attempting to retrieve the category name directly from the exercise data, and if not available, using the category ID to look up the category name from the cached category information for use in mapping exercises to categories within the application when users access exercise information through the relevant endpoints
        Object catObj = ex.get("category");

        if (catObj instanceof Map) {
            Object nameObj = ((Map<?, ?>) catObj).get("name");
            if (nameObj != null) return asString(nameObj);

            Integer id = asInt(((Map<?, ?>) catObj).get("id"));
            if (id != null) return categoryIdToName.get(id);
        }

        Integer id = asInt(catObj);
        if (id != null) return categoryIdToName.get(id);

        return null;
    }

    private List<String> extractMuscleNames(Map<String, Object> ex) { // Helper method to extract the names of muscles involved in an exercise from the raw exercise information returned by the wger API, accepting a map representing the exercise information, processing the muscle data which may be represented as either a list of muscle objects or a list of muscle IDs, and returning a list of muscle names for use in mapping exercises to involved muscles within the application when users access exercise information through the relevant endpoints
        List<String> out = new ArrayList<>();
        collectMuscles(out, ex.get("muscles"));
        collectMuscles(out, ex.get("muscles_secondary"));
        return out;
    }

    private void collectMuscles(List<String> out, Object musclesObj) { // Helper method to collect muscle names from the raw muscle data returned by the wger API, accepting a list to store the collected muscle names and an object representing the muscle data which may be in different formats (list of muscle objects or list of muscle IDs), processing the data to extract muscle names using either direct name retrieval or ID lookup from the cached muscle information, and adding the valid muscle names to the provided list for use in mapping exercises to involved muscles within the application when users access exercise information through the relevant endpoints
        if (!(musclesObj instanceof List)) return;

        for (Object o : (List<?>) musclesObj) {
            if (o instanceof Map) {
                String name = asString(((Map<?, ?>) o).get("name"));
                if (name != null && !name.isBlank()) out.add(name);
                else {
                    Integer id = asInt(((Map<?, ?>) o).get("id"));
                    if (id != null) {
                        String n = muscleIdToName.get(id);
                        if (n != null && !n.isBlank()) out.add(n);
                    }
                }
            } else {
                Integer id = asInt(o);
                if (id != null) {
                    String name = muscleIdToName.get(id);
                    if (name != null && !name.isBlank()) out.add(name);
                }
            }
        }
    }

    private List<Map<String, Object>> fetchExerciseInfoPages(int maxItems) { // Helper method to fetch exercise information from the wger API in a paginated manner, accepting a maximum number of items to retrieve, making repeated API calls to fetch pages of exercise information until the specified limit is reached or there are no more pages to fetch, and returning a list of maps representing the raw exercise information for use in processing and mapping exercises within the application when users access exercise information through the relevant endpoints
        List<Map<String, Object>> out = new ArrayList<>();
        String next = "/exerciseinfo/?language=" + languageId + "&limit=100";

        while (next != null && out.size() < maxItems) {
            Map<String, Object> page = wger.get().uri(next).retrieve().body(Map.class);
            if (page == null) break;

            Object resultsObj = page.get("results");
            if (resultsObj instanceof List) {
                for (Object o : (List<?>) resultsObj) {
                    if (o instanceof Map) {
                        out.add((Map<String, Object>) o);
                        if (out.size() >= maxItems) break;
                    }
                }
            }

            Object nextObj = page.get("next");
            next = (nextObj instanceof String) ? stripBase((String) nextObj) : null;
        }

        return out;
    }
    private String pickTranslatedField(Map<String, Object> ex, String field) { // Helper method to pick a translated field value from the raw exercise information returned by the wger API, accepting a map representing the exercise information and the field name to retrieve, attempting to retrieve the field value directly from the exercise data, and if not available, looking through the translations provided in the exercise data to find a translation for the specified language ID, returning the appropriate field value for use in mapping exercises to their translated names and descriptions within the application when users access exercise information through the relevant endpoints
        String top = asString(ex.get(field));
        if (top != null && !top.isBlank()) return top;

        Object translationsObj = ex.get("translations");
        if (!(translationsObj instanceof List)) return top;

        List<?> translations = (List<?>) translationsObj;

        for (Object t : translations) {
            if (!(t instanceof Map)) continue;
            Map<?, ?> tr = (Map<?, ?>) t;

            Integer lang = asInt(tr.get("language"));
            if (lang != null && lang == languageId) {
                String v = asString(tr.get(field));
                if (v != null && !v.isBlank()) return v;
            }
        }

        for (Object t : translations) {
            if (!(t instanceof Map)) continue;
            Map<?, ?> tr = (Map<?, ?>) t;

            String v = asString(tr.get(field));
            if (v != null && !v.isBlank()) return v;
        }

        return top;
        }


    private String stripBase(String nextUrl) { // Helper method to strip the base URL from a given URL string, accepting a URL string as a parameter, checking if the URL contains the expected base path for the wger API, and if so, removing the base portion to return a relative path that can be used for subsequent API calls within the application when processing paginated responses from the wger API
        if (nextUrl == null) return null;
        int idx = nextUrl.indexOf("/api/v2/");
        if (idx >= 0) return nextUrl.substring(idx + "/api/v2".length());
        return nextUrl;
    }

    private String firstImageUrl(Map<String, Object> ex) { // Helper method to extract the first image URL for an exercise from the raw exercise information returned by the wger API, accepting a map representing the exercise information, processing the images data which may contain multiple images, and returning the URL of the first valid image for use in mapping exercises to their representative images within the application when users access exercise information through the relevant endpoints
        Object imagesObj = ex.get("images");
        if (!(imagesObj instanceof List)) return null;

        for (Object o : (List<?>) imagesObj) {
            if (o instanceof Map) {
                String url = asString(((Map<?, ?>) o).get("image"));
                if (url != null && !url.isBlank()) return url;
            }
        }
        return null;
    }

    private List<String> extractImageUrls(Map<String, Object> ex) { // Helper method to extract all valid image URLs for an exercise from the raw exercise information returned by the wger API, accepting a map representing the exercise information, processing the images data which may contain multiple images, and returning a list of URLs for all valid images associated with the exercise for use in mapping exercises to their representative images within the application when users access exercise information through the relevant endpoints
        List<String> urls = new ArrayList<>();
        Object imagesObj = ex.get("images");
        if (!(imagesObj instanceof List)) return urls;

        for (Object o : (List<?>) imagesObj) {
            if (o instanceof Map) {
                String url = asString(((Map<?, ?>) o).get("image"));
                if (url != null && !url.isBlank()) urls.add(url);
            }
        }
        return urls;
    }

    private List<String> extractNamedList(Map<String, Object> ex, String key) { // Helper method to extract a list of names from a specified key in the raw exercise information returned by the wger API, accepting a map representing the exercise information and the key to look for, processing the data which may be in the form of a list of objects containing name fields, and returning a list of names for use in mapping exercises to their associated equipment or other attributes within the application when users access exercise information through the relevant endpoints
        List<String> out = new ArrayList<>();
        Object arr = ex.get(key);
        if (!(arr instanceof List)) return out;

        for (Object o : (List<?>) arr) {
            if (o instanceof Map) {
                String name = asString(((Map<?, ?>) o).get("name"));
                if (name != null && !name.isBlank()) out.add(name);
            }
        }
        return out;
    }

    private boolean looksNonLatin(String s) { // Helper method to check if a given string contains characters from the Cyrillic Unicode block, accepting a string as a parameter, iterating through each character in the string, and returning true if any character belongs to the Cyrillic Unicode block, which can be used as a heuristic to filter out exercises with non-Latin names when processing exercise information from the wger API within the application when users access exercise information through the relevant endpoints
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.CYRILLIC) return true;
        }
        return false;
    }

    private Integer asInt(Object o) { // Helper method to safely convert an object to an integer, accepting an object as a parameter, and returning the integer value if the object is an instance of Integer or Number, or if it is a String that can be parsed as an integer, while returning null for any other cases to facilitate consistent data processing when extracting numeric values from the raw exercise information returned by the wger API within the application when users access exercise information through the relevant endpoints
        if (o instanceof Integer integer) return integer;
        if (o instanceof Number number) return number.intValue();
        if (o instanceof String string) {
            try { return Integer.valueOf(string); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String asString(Object o) { // Helper method to safely convert an object to a string, accepting an object as a parameter, and returning a trimmed string representation of the object while treating null values and empty strings as null for consistent data processing when extracting string values from the raw exercise information returned by the wger API within the application when users access exercise information through the relevant endpoints
        return (o == null) ? null : String.valueOf(o);
    }
}
