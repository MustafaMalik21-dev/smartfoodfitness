package com.mustafa.smartfoodfitness.service;

import com.mustafa.smartfoodfitness.dto.FriendDto;
import com.mustafa.smartfoodfitness.dto.TopLiftDto;
import com.mustafa.smartfoodfitness.dto.UserSearchResultDto;
import com.mustafa.smartfoodfitness.entity.FriendRequest;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WeightEntry;
import com.mustafa.smartfoodfitness.entity.WorkoutLog;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.repository.FriendRequestRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WeightEntryRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutLogRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRequestRepository friendRepo;
    private final UserProfileRepository profileRepo;
    private final WeightEntryRepository weightRepo;
    private final WorkoutLogRepository workoutRepo;
    private final WorkoutPlanRepository workoutPlanRepo;
    private final ObjectMapper objectMapper;

    public FriendService(FriendRequestRepository friendRepo,
                         UserProfileRepository profileRepo,
                         WeightEntryRepository weightRepo,
                         WorkoutLogRepository workoutRepo,
                         WorkoutPlanRepository workoutPlanRepo,
                         ObjectMapper objectMapper) {
        this.friendRepo      = friendRepo;
        this.profileRepo     = profileRepo;
        this.weightRepo      = weightRepo;
        this.workoutRepo     = workoutRepo;
        this.workoutPlanRepo = workoutPlanRepo;
        this.objectMapper    = objectMapper;
    }

    // ── Search users by display name or email ────────────────────────────────
    @Transactional(readOnly = true)
    public List<UserSearchResultDto> searchUsers(Long requestingUserId, String query) {
        String q = "%" + query.toLowerCase() + "%";
        List<UserProfile> results = profileRepo.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return results.stream()
            .filter(p -> !p.getId().equals(requestingUserId))
            .limit(20)
            .map(p -> {
                UserSearchResultDto dto = new UserSearchResultDto();
                dto.setUserId(p.getId());
                dto.setDisplayName(p.getDisplayName());
                dto.setEmail(p.getEmail());
                Optional<FriendRequest> existing = friendRepo.findBetween(requestingUserId, p.getId());
                if (existing.isEmpty()) {
                    dto.setFriendStatus(null);
                } else {
                    FriendRequest fr = existing.get();
                    if ("ACCEPTED".equals(fr.getStatus())) {
                        dto.setFriendStatus("ACCEPTED");
                    } else if (fr.getSender().getId().equals(requestingUserId)) {
                        dto.setFriendStatus("PENDING_SENT");
                    } else {
                        dto.setFriendStatus("PENDING_RECEIVED");
                    }
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    // ── Send a friend request ────────────────────────────────────────────────
    @Transactional
    public FriendDto sendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add yourself.");
        friendRepo.findBetween(senderId, receiverId).ifPresent(fr -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already exists.");
        });
        UserProfile sender   = profileRepo.findById(senderId)  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found."));
        UserProfile receiver = profileRepo.findById(receiverId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found."));
        FriendRequest fr = new FriendRequest();
        fr.setSender(sender); fr.setReceiver(receiver); fr.setStatus("PENDING");
        Instant now = Instant.now();
        fr.setCreatedAt(now); fr.setUpdatedAt(now);
        return toDto(friendRepo.save(fr), senderId);
    }

    // ── Respond to a friend request ─────────────────────────────────────────
    @Transactional
    public FriendDto respondToRequest(Long requestId, Long respondingUserId, boolean accept) {
        FriendRequest fr = friendRepo.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        if (!fr.getReceiver().getId().equals(respondingUserId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request.");
        fr.setStatus(accept ? "ACCEPTED" : "DECLINED");
        fr.setUpdatedAt(Instant.now());
        return toDto(friendRepo.save(fr), respondingUserId);
    }

    // ── Remove a friend or cancel a request ─────────────────────────────────
    @Transactional
    public void removeFriend(Long requestId, Long userId) {
        FriendRequest fr = friendRepo.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        if (!fr.getSender().getId().equals(userId) && !fr.getReceiver().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your friend.");
        friendRepo.delete(fr);
    }

    // ── Get friends list (accepted) ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<FriendDto> getFriends(Long userId) {
        return friendRepo.findFriends(userId).stream()
            .map(fr -> toDto(fr, userId))
            .collect(Collectors.toList());
    }

    // ── Get pending requests ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<FriendDto> getPendingRequests(Long userId) {
        List<FriendRequest> incoming = friendRepo.findByReceiverIdAndStatus(userId, "PENDING");
        List<FriendRequest> outgoing = friendRepo.findBySenderIdAndStatus(userId, "PENDING");
        List<FriendDto> result = new java.util.ArrayList<>();
        incoming.forEach(fr -> { FriendDto d = toDto(fr, userId); d.setDirection("INCOMING"); result.add(d); });
        outgoing.forEach(fr -> { FriendDto d = toDto(fr, userId); d.setDirection("OUTGOING"); result.add(d); });
        return result;
    }

    // ── Helper: map FriendRequest → FriendDto ────────────────────────────────
    private FriendDto toDto(FriendRequest fr, Long viewingUserId) {
        // The "other" user from the viewing user's perspective
        UserProfile other = fr.getSender().getId().equals(viewingUserId)
            ? fr.getReceiver() : fr.getSender();

        FriendDto dto = new FriendDto();
        dto.setRequestId(fr.getId());
        dto.setUserId(other.getId());
        dto.setDisplayName(other.getDisplayName());
        dto.setEmail(other.getEmail());
        dto.setStatus(fr.getStatus());
        dto.setProfileVisibility(other.getProfileVisibility());
        dto.setShareWeight(other.getShareWeight());
        dto.setShareActivity(other.getShareActivity());

        // Only populate shared stats for accepted friends
        if ("ACCEPTED".equals(fr.getStatus())) {
            boolean canSeeWeight   = Boolean.TRUE.equals(other.getShareWeight());
            boolean canSeeActivity = Boolean.TRUE.equals(other.getShareActivity());

            if (canSeeWeight) {
                weightRepo.findTopByUserProfileIdOrderByRecordedAtDesc(other.getId()).ifPresent(we -> {
                    dto.setLatestWeightKg(we.getWeightValue());
                    dto.setLatestBmi(we.getBmi());
                    dto.setLatestBodyFatPercent(we.getBodyFatPercent());
                });
            }
            if (canSeeActivity) {
                Instant now     = Instant.now();
                Instant weekAgo = now.minus(7, ChronoUnit.DAYS);

                int thisWeek = workoutRepo
                    .findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(other.getId(), weekAgo, now)
                    .size();
                dto.setWorkoutsThisWeek(thisWeek);

                dto.setTotalWorkouts((int) workoutRepo.countByUserProfileId(other.getId()));

                Long planId = other.getSelectedWorkoutPlanId();
                if (planId != null) {
                    workoutPlanRepo.findById(planId).ifPresent(plan -> {
                        dto.setActivePlanName(plan.getTitle());
                        dto.setActivePlanSplit(plan.getSplit());
                        dto.setActivePlanDaysPerWeek(plan.getDaysPerWeek());
                    });
                }

                Instant ninetyDaysAgo = now.minus(90, ChronoUnit.DAYS);
                List<WorkoutLog> recentLogs = workoutRepo
                    .findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(
                        other.getId(), ninetyDaysAgo, now);
                dto.setTopLifts(parseTopLifts(recentLogs));
            }
        }
        return dto;
    }

    private List<TopLiftDto> parseTopLifts(List<WorkoutLog> logs) {
        Map<String, TopLiftDto> best = new LinkedHashMap<>();

        for (WorkoutLog log : logs) {
            String json = log.getDetailsJson();
            if (json == null || json.isBlank()) continue;
            try {
                JsonNode root      = objectMapper.readTree(json);
                JsonNode exercises = root.path("exercises");
                if (!exercises.isArray()) continue;

                for (JsonNode ex : exercises) {
                    String name = ex.path("name").asText(null);
                    if (name == null || name.isBlank()) continue;
                    JsonNode sets = ex.path("sets");
                    if (!sets.isArray()) continue;

                    for (JsonNode set : sets) {
                        if (!set.path("done").asBoolean(false)) continue;
                        String weightStr = set.path("weight").asText("").trim();
                        String repsStr   = set.path("reps").asText("").trim();
                        if (weightStr.isEmpty()) continue;

                        try {
                            double weight = Double.parseDouble(weightStr);
                            int    reps   = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
                            String key    = name.toLowerCase();
                            TopLiftDto existing = best.get(key);
                            if (existing == null || weight > existing.getMaxWeightKg()) {
                                best.put(key, new TopLiftDto(name, weight, reps, log.getPerformedAt()));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }

        return best.values().stream()
            .sorted(Comparator.comparingDouble(TopLiftDto::getMaxWeightKg).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }
}
