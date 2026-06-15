package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateWaterEntryRequest;
import com.mustafa.smartfoodfitness.dto.WaterEntryResponse;
import com.mustafa.smartfoodfitness.dto.WaterTrendPointResponse;
import com.mustafa.smartfoodfitness.dto.WaterTrendResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WaterEntry;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WaterEntryRepository;

@Service
public class WaterEntryService {

    private final WaterEntryRepository waterEntryRepository;
    private final UserProfileRepository userProfileRepository;

    public WaterEntryService(WaterEntryRepository waterEntryRepository,
                             UserProfileRepository userProfileRepository) {
        this.waterEntryRepository = waterEntryRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public WaterEntryResponse logWater(CreateWaterEntryRequest request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }
        if (request.getWaterMl() == null || request.getWaterMl() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "waterMl must be positive.");
        }

        UserProfile user = userProfileRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        WaterEntry e = new WaterEntry();
        e.setUserProfile(user);
        e.setWaterMl(request.getWaterMl());
        e.setLoggedAt(request.getLoggedAt() != null ? request.getLoggedAt() : Instant.now());

        Instant now = Instant.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toResponse(waterEntryRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<WaterEntryResponse> getEntriesForUser(long userId) {
        return waterEntryRepository.findByUserProfileIdOrderByLoggedAtAsc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public double getTodayTotal(long userId, String timezone) {
        ZoneId zone = safeZone(timezone);
        LocalDate today = LocalDate.now(zone);
        Instant from = today.atStartOfDay(zone).toInstant();
        Instant to   = today.plusDays(1).atStartOfDay(zone).toInstant();
        return waterEntryRepository
                .findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, to)
                .stream().mapToDouble(WaterEntry::getWaterMl).sum();
    }

    @Transactional(readOnly = true)
    public WaterTrendResponse getWaterTrend(long userId, String range, String timezone) {
        ZoneId zone = safeZone(timezone);
        LocalDate today = LocalDate.now(zone);

        List<LocalDate> days = new ArrayList<>();
        if ("week".equalsIgnoreCase(range)) {
            for (int i = 6; i >= 0; i--) days.add(today.minusDays(i));
        } else if ("month".equalsIgnoreCase(range)) {
            for (int i = 29; i >= 0; i--) days.add(today.minusDays(i));
        } else { // year — monthly buckets
            for (int i = 11; i >= 0; i--) days.add(today.withDayOfMonth(1).minusMonths(i));
        }

        Instant from = days.get(0).atStartOfDay(zone).toInstant();
        Instant to   = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<WaterEntry> entries = waterEntryRepository
                .findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, to);

        // Map date-string → total ml
        Map<String, Double> totals = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if ("year".equalsIgnoreCase(range)) {
            // Monthly totals
            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
            for (WaterEntry e : entries) {
                String key = e.getLoggedAt().atZone(zone).format(monthFmt);
                totals.merge(key, e.getWaterMl(), Double::sum);
            }
            List<WaterTrendPointResponse> pts = new ArrayList<>();
            for (LocalDate d : days) {
                String key = d.format(monthFmt);
                pts.add(new WaterTrendPointResponse(d.format(fmt), totals.getOrDefault(key, 0.0)));
            }
            return new WaterTrendResponse(pts);
        } else {
            // Daily totals
            for (WaterEntry e : entries) {
                String key = e.getLoggedAt().atZone(zone).toLocalDate().format(fmt);
                totals.merge(key, e.getWaterMl(), Double::sum);
            }
            List<WaterTrendPointResponse> pts = new ArrayList<>();
            for (LocalDate d : days) {
                String key = d.format(fmt);
                pts.add(new WaterTrendPointResponse(key, totals.getOrDefault(key, 0.0)));
            }
            return new WaterTrendResponse(pts);
        }
    }

    @Transactional
    public void deleteEntry(long id) {
        if (!waterEntryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Water entry not found.");
        }
        waterEntryRepository.deleteById(id);
    }

    private WaterEntryResponse toResponse(WaterEntry e) {
        WaterEntryResponse r = new WaterEntryResponse();
        r.setId(e.getId());
        r.setUserId(e.getUserProfile().getId());
        r.setWaterMl(e.getWaterMl());
        r.setLoggedAt(e.getLoggedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    private ZoneId safeZone(String tz) {
        if (tz == null || tz.isBlank()) return ZoneId.of("UTC");
        try { return ZoneId.of(tz); } catch (Exception e) { return ZoneId.of("UTC"); }
    }
}
