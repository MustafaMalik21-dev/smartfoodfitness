package com.mustafa.smartfoodfitness.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.service.MealDbService;
import com.mustafa.smartfoodfitness.service.MealDbService.RecipeDetail;
import com.mustafa.smartfoodfitness.service.MealDbService.RecipeSummary;

@RestController
@RequestMapping("/api/recipes") //Controller class responsible for handling HTTP requests related to recipes, providing endpoints for searching recipes, retrieving recipes by category or ingredient, fetching random recipes, and looking up detailed information about specific recipes using the MealDb API when users access the relevant endpoints in the application
public class RecipesController {

    private final MealDbService mealDb;

    public RecipesController(MealDbService mealDb) {
        this.mealDb = mealDb;
    }

    @GetMapping("/search") // handle HTTP GET requests to search for recipes in the MealDb API based on user queries, accepting a query parameter for the search term, and returning a list of response DTOs representing the matching recipes to the client when they access the relevant endpoint in the application
    public List<RecipeSummary> search(@RequestParam String q) {
        return mealDb.search(q);
    }

    @GetMapping("/category/{category}") // handle HTTP GET requests to retrieve recipes from the MealDb API based on a specified category, accepting a path variable representing the category name, and returning a list of response DTOs representing the recipes in that category to the client when they access the relevant endpoint in the application
    public List<RecipeSummary> byCategory(@PathVariable String category) {
        return mealDb.byCategory(category);
    }

    @GetMapping("/ingredient/{ingredient}") // handle HTTP GET requests to retrieve recipes from the MealDb API based on a specified ingredient, accepting a path variable representing the ingredient name, and returning a list of response DTOs representing the recipes that include that ingredient to the client when they access the relevant endpoint in the application
    public List<RecipeSummary> byIngredient(@PathVariable String ingredient) {
        return mealDb.byIngredient(ingredient);
    }

    @GetMapping("/random") // handle HTTP GET requests to retrieve random recipes from the MealDb API, and returning a list of response DTOs representing the random recipes to the client when they access the relevant endpoint in the application
    public List<RecipeSummary> random() {
        return mealDb.random();
    }

    @GetMapping("/{mealId}") // handle HTTP GET requests to retrieve detailed information about a specific recipe from the MealDb API, accepting a path variable representing the meal ID, and returning a response DTO containing the recipe details to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the recipe does not exist
    public RecipeDetail lookup(@PathVariable String mealId) {
        RecipeDetail d = mealDb.lookup(mealId);
        if (d == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found.");
        return d;
    }
}
