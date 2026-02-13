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

@RestController
@RequestMapping("/api/exercises")
public class ExerciseDatabaseController {

    private final WgerExerciseService wgerExerciseService;

    public ExerciseDatabaseController(WgerExerciseService wgerExerciseService) {
        this.wgerExerciseService = wgerExerciseService;
    }

    @GetMapping("/encyclopedia")
    public WgerEncyclopediaResponse encyclopedia(@RequestParam(defaultValue = "8") int perCategoryLimit) {
        return wgerExerciseService.getEncyclopedia(perCategoryLimit);
    }

    @GetMapping("/{id}")
    public WgerExerciseDetailResponse exercise(@PathVariable int id) {
        return wgerExerciseService.getExerciseDetail(id);
    }

    @GetMapping("/search")
    public List<WgerExerciseTile> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "60") int limit,
            @RequestParam(required = false, defaultValue = "all") String scope
    ) {
        return wgerExerciseService.search(q, limit, scope);
    }
}
