package com.mustafa.smartfoodfitness.dto;
//Define the CalorieTrendResponse DTO with fields for user ID, date range, timezone, and a list of calorie trend points, along with getter and setter methods for each field to facilitate data transfer of calorie trend information between the backend and frontend of the application
import java.util.List;

public class CalorieTrendResponse {

    private Long userId;

    private String range;
    private String timezone;

    private String fromDate;
    private String toDate;

    private List<CalorieTrendPointResponse> points;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public List<CalorieTrendPointResponse> getPoints() {
        return points;
    }

    public void setPoints(List<CalorieTrendPointResponse> points) {
        this.points = points;
    }
}
