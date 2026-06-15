package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class MicroTrendResponse {

    private Long userId;
    private String range;
    private String timezone;
    private String fromDate;
    private String toDate;
    private List<MicroTrendPointResponse> points;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public List<MicroTrendPointResponse> getPoints() { return points; }
    public void setPoints(List<MicroTrendPointResponse> points) { this.points = points; }
}
