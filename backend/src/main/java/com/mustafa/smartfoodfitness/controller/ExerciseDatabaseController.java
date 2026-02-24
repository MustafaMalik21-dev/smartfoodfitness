package com.mustafa.smartfoodfitness.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.WgerEncyclopediaResponse;
import com.mustafa.smartfoodfitness.dto.WgerExerciseDetailResponse;
import com.mustafa.smartfoodfitness.dto.WgerExerciseTile;
import com.mustafa.smartfoodfitness.service.WgerExerciseService;

//Controller class responsible for handling HTTP requests related to exercises, providing endpoints for retrieving exercise information from the wger API, including an encyclopedia of exercises, detailed information about specific exercises, and search functionality for finding exercises based on user queries when they access the relevant endpoints in the application

@RestController
@RequestMapping("/api/exercises")
public class ExerciseDatabaseController {

    private final WgerExerciseService wgerExerciseService;

    public ExerciseDatabaseController(WgerExerciseService wgerExerciseService) {
        this.wgerExerciseService = wgerExerciseService;
    }

    @GetMapping("/encyclopedia") // handle HTTP GET requests to retrieve an encyclopedia of exercises from the wger API, accepting an optional query parameter to specify the number of exercises to return per category, and returning a response DTO containing the exercise information to the client when they access the relevant endpoint in the application
    public WgerEncyclopediaResponse encyclopedia(@RequestParam(defaultValue = "8") int perCategoryLimit) {
        return wgerExerciseService.getEncyclopedia(perCategoryLimit);
    }

    @GetMapping("/{id}") // handle HTTP GET requests to retrieve detailed information about a specific exercise from the wger API, accepting a path variable representing the exercise ID, and returning a response DTO containing the exercise details to the client when they access the relevant endpoint in the application
    public WgerExerciseDetailResponse exercise(@PathVariable int id) {
        return wgerExerciseService.getExerciseDetail(id);
    }

    @GetMapping("/search") // handle HTTP GET requests to search for exercises in the wger API based on user queries, accepting query parameters for the search term, result limit, and search scope, and returning a list of response DTOs representing the matching exercises to the client when they access the relevant endpoint in the application
    public List<WgerExerciseTile> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "60") int limit,
            @RequestParam(required = false, defaultValue = "all") String scope
    ) {
        return wgerExerciseService.search(q, limit, scope);
    }
}
