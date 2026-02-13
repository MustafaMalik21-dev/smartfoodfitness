package com.mustafa.smartfoodfitness.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CalorieTrendPointResponse;
import com.mustafa.smartfoodfitness.dto.CalorieTrendResponse;
import com.mustafa.smartfoodfitness.dto.MacroTrendPointResponse;
import com.mustafa.smartfoodfitness.dto.MacroTrendResponse;
import com.mustafa.smartfoodfitness.dto.NutritionSummaryResponse;
import com.mustafa.smartfoodfitness.dto.NutritionSummaryVsGoalsResponse;
import com.mustafa.smartfoodfitness.dto.NutritionTrendPointResponse;
import com.mustafa.smartfoodfitness.dto.NutritionTrendResponse;
import com.mustafa.smartfoodfitness.entity.FoodEntryLogs;
import com.mustafa.smartfoodfitness.entity.UserGoals;
import com.mustafa.smartfoodfitness.repository.FoodEntryLogsRepository;
import com.mustafa.smartfoodfitness.repository.UserGoalsRepository;

@Service
public class NutritionSummaryService {

    private final FoodEntryLogsRepository foodEntryLogsRepository;
    private final UserGoalsRepository userGoalsRepository;

    public NutritionSummaryService( // constructor injection of repositories
            FoodEntryLogsRepository foodEntryLogsRepository,
            UserGoalsRepository userGoalsRepository
    ) {
        this.foodEntryLogsRepository = foodEntryLogsRepository;
        this.userGoalsRepository = userGoalsRepository;
    }

    private int calculatePercent(int total, int goal) { // helper method to calculate the percentage of a total value relative to a goal, returning 0 if the goal is zero or negative to avoid division by zero, otherwise calculating the percentage as (total * 100.0) / goal and rounding it to the nearest integer before returning it
        if (goal <= 0) return 0;
        double pct = (total * 100.0) / goal;
        return (int) Math.round(pct);
    }

    public NutritionSummaryVsGoalsResponse getNutritionSummaryVsGoals(long userId, String period, String date, String timezone) { // retrieve a nutrition summary for a user and compare it against the user's goals, first fetching the nutrition summary for the specified user and time period using the getNutritionSummary method, then fetching the user's goals from the database, calculating the remaining calories, proteins, carbs, and fats by subtracting the totals from the goals, calculating the percentage of each total relative to its corresponding goal using the calculatePercent helper method, and returning a response DTO that includes all of this information
        NutritionSummaryResponse summary = getNutritionSummary(userId, period, date, timezone);

        UserGoals goals = userGoalsRepository.findByUserProfileId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User goals not found."));

        int totalCalories = safeInt(summary.getTotalCalories());
        int totalProteins = safeInt(summary.getTotalProteins());
        int totalCarbs = safeInt(summary.getTotalCarbs());
        int totalFats = safeInt(summary.getTotalFats());

        int caloriesGoal = safeInt(goals.getCalorieGoal());
        int proteinsGoal = safeInt(goals.getProteinGoal());
        int carbsGoal = safeInt(goals.getCarbGoal());
        int fatsGoal = safeInt(goals.getFatGoal());

        NutritionSummaryVsGoalsResponse r = new NutritionSummaryVsGoalsResponse(); // create a new NutritionSummaryVsGoalsResponse DTO and populate it with data from the nutrition summary and user goals, including the total calories, proteins, carbs, and fats, the corresponding goals for each nutrient, the remaining amounts (calculated as goal - total), and the percentage of each total relative to its goal
        r.setUserId(userId);
        r.setPeriod(summary.getPeriod());
        r.setFrom(summary.getFrom());
        r.setTo(summary.getTo());
        r.setEntriesCount(summary.getEntriesCount());

        r.setTotalCalories(totalCalories);
        r.setCaloriesGoal(caloriesGoal);
        r.setCaloriesRemaining(caloriesGoal - totalCalories);
        r.setCaloriesPercent(calculatePercent(totalCalories, caloriesGoal));

        r.setTotalProteins(totalProteins);
        r.setProteinsGoal(proteinsGoal);
        r.setProteinsRemaining(proteinsGoal - totalProteins);
        r.setProteinsPercent(calculatePercent(totalProteins, proteinsGoal));

        r.setTotalCarbs(totalCarbs);
        r.setCarbsGoal(carbsGoal);
        r.setCarbsRemaining(carbsGoal - totalCarbs);
        r.setCarbsPercent(calculatePercent(totalCarbs, carbsGoal));

        r.setTotalFats(totalFats);
        r.setFatsGoal(fatsGoal);
        r.setFatsRemaining(fatsGoal - totalFats);
        r.setFatsPercent(calculatePercent(totalFats, fatsGoal));

        return r;
    }

    @SuppressWarnings("ConvertToStringSwitch") // retrieve a nutrition summary for a user based on the specified time period and date, first validating and parsing the input parameters to determine the date range for the summary, then fetching the relevant food entry logs from the database for the specified user and date range, calculating the total calories, proteins, carbs, and fats from the retrieved entries while safely handling null values using the safeInt helper method, and returning a response DTO that includes all of this information along with the original input parameters
    public NutritionSummaryResponse getNutritionSummary(long userId, String period, String date, String timezone) {
        String periodValue = (period == null || period.isBlank()) ? "daily" : period.trim().toLowerCase();

        ZoneId zoneId; // validate and parse the timezone parameter to create a ZoneId, defaulting to the system timezone if the parameter is null or blank, and throwing a bad request error if the provided timezone string is invalid
        try {
            zoneId = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone.");
        }

        LocalDate baseDate; // validate and parse the date parameter to create a LocalDate, defaulting to the current date in the specified timezone if the parameter is null or blank, and throwing a bad request error if the provided date string is invalid
        try {
            baseDate = (date == null || date.isBlank()) // default to current date in the specified timezone if not provided or blank, otherwise parse the provided date string into a LocalDate
                    ? LocalDate.now(zoneId)
                    : LocalDate.parse(date.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date. Use YYYY-MM-DD.");
        }

        Instant from;
        Instant toExclusive;

        if (periodValue.equals("daily")) { // determine the date range for the summary based on the specified period value, calculating the from and toExclusive Instants that represent the start and end of the date range for the summary, with different logic for daily, weekly, and monthly periods, and throwing a bad request error if the provided period value is invalid
            from = baseDate.atStartOfDay(zoneId).toInstant();
            toExclusive = baseDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        } else if (periodValue.equals("weekly")) {
            LocalDate weekStart = baseDate.with(DayOfWeek.MONDAY);
            from = weekStart.atStartOfDay(zoneId).toInstant();
            toExclusive = weekStart.plusDays(7).atStartOfDay(zoneId).toInstant();
        } else if (periodValue.equals("monthly")) {
            LocalDate monthStart = baseDate.with(TemporalAdjusters.firstDayOfMonth());
            from = monthStart.atStartOfDay(zoneId).toInstant();
            toExclusive = monthStart.plusMonths(1).atStartOfDay(zoneId).toInstant();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period. Use daily, weekly, or monthly.");
        }

        List<FoodEntryLogs> entries =
                foodEntryLogsRepository.findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, toExclusive);

        int calories = 0;
        int proteins = 0;
        int carbs = 0;
        int fats = 0;

        for (FoodEntryLogs e : entries) { // iterate through the retrieved food entry logs and accumulate the total calories, proteins, carbs, and fats while safely handling null values using the safeInt helper method, which returns 0 for null values to ensure that the totals are accurate even if some entries have missing nutrient information
            calories += safeInt(e.getCalories()); 
            proteins += safeInt(e.getProteins());
            carbs += safeInt(e.getCarbs());
            fats += safeInt(e.getFats());
        }

        NutritionSummaryResponse response = new NutritionSummaryResponse(); // create a new NutritionSummaryResponse DTO and populate it with data from the input parameters and the calculated totals, including the user ID, period, date range, total calories, proteins, carbs, fats, and the count of entries that were included in the summary
        response.setUserId(userId);
        response.setPeriod(periodValue);
        response.setFrom(from);
        response.setTo(toExclusive);
        response.setTotalCalories(calories);
        response.setTotalProteins(proteins);
        response.setTotalCarbs(carbs);
        response.setTotalFats(fats);
        response.setEntriesCount(entries.size());
        return response;
    }

    private int safeInt(Integer value) { // helper method to safely convert an Integer to a primitive int, returning 0 if the input value is null to avoid NullPointerExceptions when performing arithmetic operations on nutrient values that may be missing from some food entry logs
        return value == null ? 0 : value; 
    }

    private int daysForRangeOrThrow(String rangeValue) { // helper method to convert a range string (e.g., "week", "month", "year") into a corresponding number of days, throwing a bad request error if the provided range value is invalid
        if (rangeValue.equals("week")) return 7;
        if (rangeValue.equals("month")) return 30;
        if (rangeValue.equals("year")) return 365;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range. Use week, month, or year.");
    }

    public NutritionTrendResponse getNutritionTrend(long userId, String range, String date, String timezone) { // retrieve a nutrition trend for a user based on the specified range, date, and timezone, first validating and parsing the input parameters to determine the date range for the trend, then fetching the relevant food entry logs from the database for the specified user and date range, calculating the total calories, proteins, carbs, and fats for each day within the date range while safely handling null values using the safeInt helper method, and returning a response DTO that includes all of this information along with the original input parameters
        String rangeValue = (range == null || range.isBlank()) ? "week" : range.trim().toLowerCase();

        ZoneId zoneId;
        try {
            zoneId = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone.");
        }

        LocalDate baseDate;
        try {
            baseDate = (date == null || date.isBlank())
                    ? LocalDate.now(zoneId)
                    : LocalDate.parse(date.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date. Use YYYY-MM-DD.");
        }

        int days = daysForRangeOrThrow(rangeValue); // determine the number of days to include in the trend based on the specified range value, using the daysForRangeOrThrow helper method to convert the range string into a corresponding number of days, and throwing a bad request error if the provided range value is invalid

        LocalDate fromDate = baseDate.minusDays(days - 1);
        LocalDate toDate = baseDate;

        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant(); // calculate the from and toExclusive Instants that represent the start and end of the date range for the trend, with fromInstant representing the start of the fromDate and toExclusive representing the start of the day after the toDate, ensuring that all entries logged on the toDate are included in the trend
        Instant toExclusive = toDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<FoodEntryLogs> entries =
                foodEntryLogsRepository.findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, fromInstant, toExclusive);

        Map<LocalDate, int[]> totalsByDay = new HashMap<>(); // create a map to accumulate the total calories, proteins, carbs, and fats for each day within the date range, where the key is a LocalDate representing the day and the value is an array of integers representing the totals for that day (index 0 for calories, 1 for proteins, 2 for carbs, and 3 for fats)
        Map<LocalDate, Integer> countByDay = new HashMap<>();

        for (FoodEntryLogs e : entries) { // iterate through the retrieved food entry logs and accumulate the total calories, proteins, carbs, and fats for each day by first converting the loggedAt timestamp of each entry into a LocalDate based on the specified timezone, then using the computeIfAbsent method to initialize an array of totals for that day if it does not already exist in the totalsByDay map, and finally adding the nutrient values from the entry to the corresponding totals in the array while safely handling null values using the safeInt helper method, and also keeping track of the count of entries for each day in the countByDay map
            Instant loggedAt = e.getLoggedAt();
            if (loggedAt == null) continue;

            LocalDate day = loggedAt.atZone(zoneId).toLocalDate();

            int[] totals = totalsByDay.computeIfAbsent(day, k -> new int[4]); // initialize an array of totals for the day if it does not already exist in the totalsByDay map, and return the existing array if it does exist, allowing us to accumulate the nutrient totals for each day without having to check for null values or manually initialize the arrays
            totals[0] += safeInt(e.getCalories());
            totals[1] += safeInt(e.getProteins());
            totals[2] += safeInt(e.getCarbs());
            totals[3] += safeInt(e.getFats());

            countByDay.put(day, countByDay.getOrDefault(day, 0) + 1);
        }

        ArrayList<NutritionTrendPointResponse> points = new ArrayList<>(); // create a list of NutritionTrendPointResponse DTOs representing the nutrition trend points for each day within the date range, iterating through each day from the fromDate to the toDate and retrieving the corresponding totals from the totalsByDay map (or using default values of 0 if no entries exist for that day), then creating a new NutritionTrendPointResponse DTO for each day and populating it with the date, total calories, proteins, carbs, fats, and entry count for that day before adding it to the points list

        for (int i = 0; i < days; i++) { // iterate through each day from the fromDate to the toDate by using a loop that runs for the number of days in the range, calculating the LocalDate for each day by adding the loop index to the fromDate, retrieving the corresponding totals from the totalsByDay map (or using default values of 0 if no entries exist for that day), creating a new NutritionTrendPointResponse DTO for each day and populating it with the date, total calories, proteins, carbs, fats, and entry count for that day before adding it to the points list
            LocalDate day = fromDate.plusDays(i);
            int[] totals = totalsByDay.getOrDefault(day, new int[] { 0, 0, 0, 0 });
            int count = countByDay.getOrDefault(day, 0);

            NutritionTrendPointResponse p = new NutritionTrendPointResponse(); // create a new NutritionTrendPointResponse DTO for the day and populate it with the date, total calories, proteins, carbs, fats, and entry count for that day before adding it to the points list
            p.setDate(day.toString());
            p.setTotalCalories(totals[0]);
            p.setTotalProteins(totals[1]);
            p.setTotalCarbs(totals[2]);
            p.setTotalFats(totals[3]);
            p.setEntriesCount(count);
            points.add(p);
        }

        NutritionTrendResponse r = new NutritionTrendResponse();// create a new NutritionTrendResponse DTO and populate it with data from the input parameters and the calculated trend points, including the user ID, range, timezone, date range, and the list of nutrition trend points for each day within the date range
        r.setUserId(userId);
        r.setRange(rangeValue);
        r.setTimezone(zoneId.getId());
        r.setFromDate(fromDate.toString());
        r.setToDate(toDate.toString());
        r.setPoints(points);

        return r;
    }

    public MacroTrendResponse getMacroTrend(long userId, String range, String date, String timezone) { // retrieve a macro trend for a user based on the specified range, date, and timezone by calling the getNutritionTrend method to fetch the nutrition trend for the user and then mapping the nutrition trend points to macro trend points that only include the total proteins, carbs, and fats (excluding calories), and returning a response DTO that includes all of this information along with the original input parameters
        String rangeValue = (range == null || range.isBlank()) ? "week" : range.trim().toLowerCase();

        ZoneId zoneId;
        try {
            zoneId = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone.");
        }

        LocalDate baseDate;
        try {
            baseDate = (date == null || date.isBlank())
                    ? LocalDate.now(zoneId)
                    : LocalDate.parse(date.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date. Use YYYY-MM-DD.");
        }

        int days = daysForRangeOrThrow(rangeValue);

        LocalDate fromDate = baseDate.minusDays(days - 1);
        LocalDate toDate = baseDate;

        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant(); // calculate the from and toExclusive Instants that represent the start and end of the date range for the trend, with fromInstant representing the start of the fromDate and toExclusive representing the start of the day after the toDate, ensuring that all entries logged on the toDate are included in the trend
        Instant toExclusive = toDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<FoodEntryLogs> entries =
                foodEntryLogsRepository.findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, fromInstant, toExclusive);

        Map<LocalDate, int[]> totalsByDay = new HashMap<>(); // create a map to accumulate the total proteins, carbs, and fats for each day within the date range, where the key is a LocalDate representing the day and the value is an array of integers representing the totals for that day (index 0 for proteins, 1 for carbs, and 2 for fats), and also create a map to keep track of the count of entries for each day
        Map<LocalDate, Integer> countByDay = new HashMap<>();

        for (FoodEntryLogs e : entries) { // iterate through the retrieved food entry logs and accumulate the total proteins, carbs, and fats for each day by first converting the loggedAt timestamp of each entry into a LocalDate based on the specified timezone, then using the computeIfAbsent method to initialize an array of totals for that day if it does not already exist in the totalsByDay map, and finally adding the nutrient values from the entry to the corresponding totals in the array while safely handling null values using the safeInt helper method, and also keeping track of the count of entries for each day in the countByDay map
            Instant loggedAt = e.getLoggedAt();
            if (loggedAt == null) continue;

            LocalDate day = loggedAt.atZone(zoneId).toLocalDate(); // convert the loggedAt timestamp of the entry into a LocalDate based on the specified timezone to determine which day the entry belongs to for the purpose of accumulating the nutrient totals for each day

            int[] totals = totalsByDay.computeIfAbsent(day, k -> new int[3]); //initialize an array of totals for the day if it does not already exist in the totalsByDay map, and return the existing array if it does exist, allowing us to accumulate the nutrient totals for each day without having to check for null values or manually initialize the arrays, with index 0 for proteins, 1 for carbs, and 2 for fats (excluding calories since this is a macro trend)
            totals[0] += safeInt(e.getProteins());
            totals[1] += safeInt(e.getCarbs());
            totals[2] += safeInt(e.getFats());

            countByDay.put(day, countByDay.getOrDefault(day, 0) + 1);
        }

        ArrayList<MacroTrendPointResponse> points = new ArrayList<>(); // create a list of MacroTrendPointResponse DTOs representing the macro trend points for each day within the date range, iterating through each day from the fromDate to the toDate and retrieving the corresponding totals from the totalsByDay map (or using default values of 0 if no entries exist for that day), then creating a new MacroTrendPointResponse DTO for each day and populating it with the date, total proteins, carbs, fats, and entry count for that day before adding it to the points list

        for (int i = 0; i < days; i++) { // iterate through each day from the fromDate to the toDate by using a loop that runs for the number of days in the range, calculating the LocalDate for each day by adding the loop index to the fromDate, retrieving the corresponding totals from the totalsByDay map (or using default values of 0 if no entries exist for that day), creating a new MacroTrendPointResponse DTO for each day and populating it with the date, total proteins, carbs, fats, and entry count for that day before adding it to the points list
            LocalDate day = fromDate.plusDays(i);
            int[] totals = totalsByDay.getOrDefault(day, new int[] { 0, 0, 0 });
            int count = countByDay.getOrDefault(day, 0);

            MacroTrendPointResponse p = new MacroTrendPointResponse(); // create a new MacroTrendPointResponse DTO for the day and populate it with the date, total proteins, carbs, fats, and entry count for that day before adding it to the points list
            p.setDate(day.toString());
            p.setProteins(totals[0]);
            p.setCarbs(totals[1]);
            p.setFats(totals[2]);
            p.setEntriesCount(count);
            points.add(p);
        }

        MacroTrendResponse r = new MacroTrendResponse(); // create a new MacroTrendResponse DTO and populate it with data from the input parameters and the calculated trend points, including the user ID, range, timezone, date range, and the list of macro trend points for each day within the date range
        r.setUserId(userId);
        r.setRange(rangeValue);
        r.setTimezone(zoneId.getId());
        r.setFromDate(fromDate.toString());
        r.setToDate(toDate.toString());
        r.setPoints(points);

        return r;
    }

    public CalorieTrendResponse getCalorieTrend(long userId, String range, String date, String timezone) {
        NutritionTrendResponse trend = getNutritionTrend(userId, range, date, timezone);

        List<CalorieTrendPointResponse> points = trend.getPoints().stream().map(p -> { // map the nutrition trend points to calorie trend points by creating a new CalorieTrendPointResponse DTO for each nutrition trend point and populating it with the date, total calories, and entry count from the corresponding nutrition trend point, effectively transforming the nutrition trend into a calorie-specific trend that only includes the total calories and entry count for each day while preserving the original date information
            CalorieTrendPointResponse cp = new CalorieTrendPointResponse();
            cp.setDate(p.getDate());
            cp.setCalories(p.getTotalCalories());
            cp.setEntriesCount(p.getEntriesCount());
            return cp;
        }).toList();

        CalorieTrendResponse r = new CalorieTrendResponse(); // create a new CalorieTrendResponse DTO and populate it with data from the input parameters and the calculated calorie trend points, including the user ID, range, timezone, date range, and the list of calorie trend points for each day within the date range
        r.setUserId(trend.getUserId());
        r.setRange(trend.getRange());
        r.setTimezone(trend.getTimezone());
        r.setFromDate(trend.getFromDate());
        r.setToDate(trend.getToDate());
        r.setPoints(points);

        return r;
    }
}
