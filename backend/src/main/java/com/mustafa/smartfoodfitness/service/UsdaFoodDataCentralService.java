package com.mustafa.smartfoodfitness.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mustafa.smartfoodfitness.dto.FoodSearchResponse;
import com.mustafa.smartfoodfitness.dto.FoodSearchResultItem;

@Service
public class UsdaFoodDataCentralService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${usda.fdc.apiKey}")
    private String apiKey;

    private static final String BASE = "https://api.nal.usda.gov/fdc/v1";

    public FoodSearchResponse searchFoods(String query, int pageSize) {
        String url = BASE + "/foods/search?api_key=" + apiKey;

        Map<String, Object> body = new HashMap<>();
        body.put("query", query == null ? "" : query.trim());
        body.put("pageSize", pageSize > 0 ? pageSize : 20);

        List<String> dataTypes = new ArrayList<>();
        dataTypes.add("Foundation");
        dataTypes.add("SR Legacy");
        dataTypes.add("Survey (FNDDS)");
        dataTypes.add("Branded");
        body.put("dataType", dataTypes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Object raw = restTemplate.postForObject(
                url,
                new HttpEntity<>(body, headers),
                Object.class
        );

        List<FoodSearchResultItem> items = new ArrayList<>();

        if (raw instanceof Map<?, ?> root) {
            Object foodsObj = root.get("foods");

            if (foodsObj instanceof List<?> foods) {
                for (Object o : foods) {
                    if (!(o instanceof Map<?, ?> m)) continue;

                    FoodSearchResultItem item = new FoodSearchResultItem();

                    Object fdcIdObj = m.get("fdcId");
                    if (fdcIdObj instanceof Number n) {
                        item.setFdcId(n.longValue());
                    }

                    String name = asString(m.get("description"));
                    String brand = asString(m.get("brandName"));
                    item.setName(name);
                    item.setBrand(brand);

                    Map<Integer, Double> nutrients = extractNutrients(m.get("foodNutrients"));
                    item.setKcalPer100g(nutrients.get(1008));    // Energy (kcal)
                    item.setProteinPer100g(nutrients.get(1003)); // Protein
                    item.setFatPer100g(nutrients.get(1004));     // Total lipid (fat)
                    item.setCarbsPer100g(nutrients.get(1005));   // Carbohydrate

                    items.add(item);
                }
            }
        }

        FoodSearchResponse out = new FoodSearchResponse();
        out.setItems(items);
        return out;
    }

    private Map<Integer, Double> extractNutrients(Object foodNutrientsObj) {
        Map<Integer, Double> map = new HashMap<>();
        if (!(foodNutrientsObj instanceof List<?> list)) return map;

        for (Object o : list) {
            if (!(o instanceof Map<?, ?> n)) continue;

            Integer nutrientId = null;
            Object nutrientIdObj = n.get("nutrientId");
            if (nutrientIdObj instanceof Number numId) {
                nutrientId = numId.intValue();
            }

            Double value = null;
            Object valueObj = n.get("value");
            if (valueObj instanceof Number numVal) {
                value = numVal.doubleValue();
            }

            if (nutrientId != null && value != null) {
                map.put(nutrientId, value);
            }
        }

        return map;
    }

    private String asString(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }
}
