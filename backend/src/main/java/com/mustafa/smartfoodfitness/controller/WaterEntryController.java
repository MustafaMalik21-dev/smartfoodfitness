package com.mustafa.smartfoodfitness.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.CreateWaterEntryRequest;
import com.mustafa.smartfoodfitness.dto.WaterEntryResponse;
import com.mustafa.smartfoodfitness.dto.WaterTrendResponse;
import com.mustafa.smartfoodfitness.service.WaterEntryService;

@RestController
@RequestMapping("/api/water-entries")
public class WaterEntryController {

    private final WaterEntryService waterEntryService;

    public WaterEntryController(WaterEntryService waterEntryService) {
        this.waterEntryService = waterEntryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaterEntryResponse logWater(@RequestBody CreateWaterEntryRequest request) {
        return waterEntryService.logWater(request);
    }

    @GetMapping("/user/{userId}")
    public List<WaterEntryResponse> getEntriesForUser(@PathVariable long userId) {
        return waterEntryService.getEntriesForUser(userId);
    }

    @GetMapping("/user/{userId}/today")
    public Map<String, Double> getTodayTotal(
            @PathVariable long userId,
            @RequestParam(required = false) String timezone) {
        double total = waterEntryService.getTodayTotal(userId, timezone);
        return Map.of("totalMl", total);
    }

    @GetMapping("/user/{userId}/trend")
    public WaterTrendResponse getWaterTrend(
            @PathVariable long userId,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String timezone) {
        return waterEntryService.getWaterTrend(userId, range != null ? range : "week", timezone);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable long id) {
        waterEntryService.deleteEntry(id);
    }
}
