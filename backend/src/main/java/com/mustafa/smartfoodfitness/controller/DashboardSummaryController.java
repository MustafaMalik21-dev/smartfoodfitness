package com.mustafa.smartfoodfitness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.DashboardSummaryResponse;
import com.mustafa.smartfoodfitness.service.DashboardSummaryService;

@RestController
@RequestMapping("/api/dashboard-summary") // define a REST controller for handling HTTP requests related to the dashboard summary, with a base URL of "/api/dashboard-summary" to group all dashboard summary-related endpoints together and allow for organized routing within the application
public class DashboardSummaryController {

    private final DashboardSummaryService dashboardSummaryService;

    public DashboardSummaryController(DashboardSummaryService dashboardSummaryService) {
        this.dashboardSummaryService = dashboardSummaryService;
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve a dashboard summary for a specific user identified by their user ID, optionally filtered by a specified date and timezone, allowing clients to view the user's dashboard summary information when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public DashboardSummaryResponse getDashboardSummary(
            @PathVariable long userId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return dashboardSummaryService.getDashboardSummary(userId, date, timezone);
    }
}
