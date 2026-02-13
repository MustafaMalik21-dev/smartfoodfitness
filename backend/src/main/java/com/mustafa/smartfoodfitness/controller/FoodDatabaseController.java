package com.mustafa.smartfoodfitness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.FoodSearchResponse;
import com.mustafa.smartfoodfitness.service.UsdaFoodDataCentralService;

@RestController
@RequestMapping("/api/food-database")
public class FoodDatabaseController {

    private final UsdaFoodDataCentralService usda;

    public FoodDatabaseController(UsdaFoodDataCentralService usda) {
        this.usda = usda;
    }

    @GetMapping("/search")
    public FoodSearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        return usda.searchFoods(q, limit);
    }
}
