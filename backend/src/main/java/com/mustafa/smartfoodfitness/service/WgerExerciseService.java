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

    public WgerExerciseService(RestClient wgerRestClient, @Value("${wger.language-id}") int languageId) {
        this.wger = wgerRestClient;
        this.languageId = languageId;
    }

    public WgerEncyclopediaResponse getEncyclopedia(int perCategoryLimit) {
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

        for (Map<String, Object> ex : all) {
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

    public List<WgerExerciseTile> search(String q, int limit, String scope) {
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

    public WgerExerciseDetailResponse getExerciseDetail(int id) {
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

    // ---------------- core mapping (FIX) ----------------

    private String mapToYourCategory(Map<String, Object> ex) {
        String name = asString(ex.get("name"));
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);

        String catName = extractCategoryName(ex);
        String c = catName == null ? "" : catName.toLowerCase(Locale.ROOT);

        List<String> muscles = extractMuscleNames(ex);
        String m = String.join(" ", muscles).toLowerCase(Locale.ROOT);

        // Cardio: wger categories vary; also catch by keywords.
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

        // Final fallback: if wger has “Abs” etc as category names
        if (c.contains("abs")) return "Core";
        if (c.contains("legs") || c.contains("calves")) return "Legs";
        if (c.contains("arms")) return "Arms";
        if (c.contains("shoulders")) return "Shoulders";
        if (c.contains("back")) return "Back";
        if (c.contains("chest")) return "Chest";

        return null;
    }

    // ---------------- caches (NOT hardcoded) ----------------

    private void ensureCategoryCache() {
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

    private void ensureMuscleCache() {
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

    private String extractCategoryName(Map<String, Object> ex) {
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

    private List<String> extractMuscleNames(Map<String, Object> ex) {
        List<String> out = new ArrayList<>();
        collectMuscles(out, ex.get("muscles"));
        collectMuscles(out, ex.get("muscles_secondary"));
        return out;
    }

    private void collectMuscles(List<String> out, Object musclesObj) {
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

    // ---------------- fetch helpers ----------------

    private List<Map<String, Object>> fetchExerciseInfoPages(int maxItems) {
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
    private String pickTranslatedField(Map<String, Object> ex, String field) {
        String top = asString(ex.get(field));
        if (top != null && !top.isBlank()) return top;

        Object translationsObj = ex.get("translations");
        if (!(translationsObj instanceof List)) return top;

        List<?> translations = (List<?>) translationsObj;

        // Prefer the requested languageId first
        for (Object t : translations) {
            if (!(t instanceof Map)) continue;
            Map<?, ?> tr = (Map<?, ?>) t;

            Integer lang = asInt(tr.get("language"));
            if (lang != null && lang == languageId) {
                String v = asString(tr.get(field));
                if (v != null && !v.isBlank()) return v;
            }
        }

        // Fallback: first non-empty translation
        for (Object t : translations) {
            if (!(t instanceof Map)) continue;
            Map<?, ?> tr = (Map<?, ?>) t;

            String v = asString(tr.get(field));
            if (v != null && !v.isBlank()) return v;
        }

        return top;
        }


    private String stripBase(String nextUrl) {
        if (nextUrl == null) return null;
        int idx = nextUrl.indexOf("/api/v2/");
        if (idx >= 0) return nextUrl.substring(idx + "/api/v2".length());
        return nextUrl;
    }

    private String firstImageUrl(Map<String, Object> ex) {
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

    private List<String> extractImageUrls(Map<String, Object> ex) {
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

    private List<String> extractNamedList(Map<String, Object> ex, String key) {
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

    private boolean looksNonLatin(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.CYRILLIC) return true;
        }
        return false;
    }

    private Integer asInt(Object o) {
        if (o instanceof Integer integer) return integer;
        if (o instanceof Number number) return number.intValue();
        if (o instanceof String string) {
            try { return Integer.valueOf(string); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String asString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
}
