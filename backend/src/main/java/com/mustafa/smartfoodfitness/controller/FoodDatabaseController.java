package com.mustafa.smartfoodfitness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.FoodSearchResponse;
import com.mustafa.smartfoodfitness.service.UsdaFoodDataCentralService;

@RestController
@RequestMapping("/api/food-database") //Controller class responsible for handling HTTP requests related to the food database, providing an endpoint for searching foods using the USDA FoodData Central API, accepting query parameters for the search term and result limit, and returning a response DTO containing the search results to the client when they access the relevant endpoint in the application
public class FoodDatabaseController {

    private final UsdaFoodDataCentralService usda;

    public FoodDatabaseController(UsdaFoodDataCentralService usda) {
        this.usda = usda;
    }

    @GetMapping("/search") // handle HTTP GET requests to search for foods in the USDA FoodData Central API based on user queries, accepting query parameters for the search term and result limit, and returning a response DTO containing the search results to the client when they access the relevant endpoint in the application
    public FoodSearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        return usda.searchFoods(q, limit);
    }
}
