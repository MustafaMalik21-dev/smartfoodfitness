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
@RequestMapping("/api/recipes")
public class RecipesController {

    private final MealDbService mealDb;

    public RecipesController(MealDbService mealDb) {
        this.mealDb = mealDb;
    }

    @GetMapping("/search")
    public List<RecipeSummary> search(@RequestParam String q) {
        return mealDb.search(q);
    }

    @GetMapping("/category/{category}")
    public List<RecipeSummary> byCategory(@PathVariable String category) {
        return mealDb.byCategory(category);
    }

    @GetMapping("/ingredient/{ingredient}")
    public List<RecipeSummary> byIngredient(@PathVariable String ingredient) {
        return mealDb.byIngredient(ingredient);
    }

    @GetMapping("/random")
    public List<RecipeSummary> random() {
        return mealDb.random();
    }

    @GetMapping("/{mealId}")
    public RecipeDetail lookup(@PathVariable String mealId) {
        RecipeDetail d = mealDb.lookup(mealId);
        if (d == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found.");
        return d;
    }
}
