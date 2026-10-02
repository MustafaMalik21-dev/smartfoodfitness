package com.mustafa.smartfoodfitness.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.CompletedWorkoutResponse;
import com.mustafa.smartfoodfitness.dto.FoodEntryLogsResponse;
import com.mustafa.smartfoodfitness.dto.MessageDto;
import com.mustafa.smartfoodfitness.dto.NotificationResponse;
import com.mustafa.smartfoodfitness.dto.UserDataExportResponse;
import com.mustafa.smartfoodfitness.dto.UserGoalsResponse;
import com.mustafa.smartfoodfitness.dto.UserProfileResponse;
import com.mustafa.smartfoodfitness.dto.WaterEntryResponse;
import com.mustafa.smartfoodfitness.dto.WeightEntryResponse;
import com.mustafa.smartfoodfitness.dto.WorkoutLogResponse;
import com.mustafa.smartfoodfitness.entity.CompletedWorkout;
import com.mustafa.smartfoodfitness.entity.FoodEntryLogs;
import com.mustafa.smartfoodfitness.entity.Message;
import com.mustafa.smartfoodfitness.entity.Notification;
import com.mustafa.smartfoodfitness.entity.UserGoals;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WaterEntry;
import com.mustafa.smartfoodfitness.entity.WeightEntry;
import com.mustafa.smartfoodfitness.entity.WorkoutLog;
import com.mustafa.smartfoodfitness.repository.CompletedWorkoutRepository;
import com.mustafa.smartfoodfitness.repository.FoodEntryLogsRepository;
import com.mustafa.smartfoodfitness.repository.FriendRequestRepository;
import com.mustafa.smartfoodfitness.repository.MessageRepository;
import com.mustafa.smartfoodfitness.repository.NotificationRepository;
import com.mustafa.smartfoodfitness.repository.UserGoalsRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WaterEntryRepository;
import com.mustafa.smartfoodfitness.repository.WeightEntryRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutLogRepository;

@Service
public class UserDataService {

    private final UserProfileRepository userProfileRepository;
    private final UserGoalsRepository userGoalsRepository;
    private final FoodEntryLogsRepository foodEntryLogsRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final WaterEntryRepository waterEntryRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final CompletedWorkoutRepository completedWorkoutRepository;
    private final NotificationRepository notificationRepository;
    private final MessageRepository messageRepository;
    private final FriendRequestRepository friendRequestRepository;

    public UserDataService(
            UserProfileRepository userProfileRepository,
            UserGoalsRepository userGoalsRepository,
            FoodEntryLogsRepository foodEntryLogsRepository,
            WeightEntryRepository weightEntryRepository,
            WaterEntryRepository waterEntryRepository,
            WorkoutLogRepository workoutLogRepository,
            CompletedWorkoutRepository completedWorkoutRepository,
            NotificationRepository notificationRepository,
            MessageRepository messageRepository,
            FriendRequestRepository friendRequestRepository
    ) {
        this.userProfileRepository = userProfileRepository;
        this.userGoalsRepository = userGoalsRepository;
        this.foodEntryLogsRepository = foodEntryLogsRepository;
        this.weightEntryRepository = weightEntryRepository;
        this.waterEntryRepository = waterEntryRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.completedWorkoutRepository = completedWorkoutRepository;
        this.notificationRepository = notificationRepository;
        this.messageRepository = messageRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    @Transactional(readOnly = true) // assemble a complete copy of everything the authenticated user owns into a single response document, reusing the same response DTOs the rest of the API already returns so the exported shapes match what the client is used to, and stamping the document with the time it was produced
    public UserDataExportResponse exportCurrentUserData() {
        Long userId = currentUserId();

        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        UserDataExportResponse export = new UserDataExportResponse();
        export.setGeneratedAt(Instant.now());
        export.setProfile(toProfileResponse(user));

        export.setGoals(userGoalsRepository.findByUserProfileId(userId)
                .map(this::toGoalsResponse)
                .orElse(null));

        export.setFoodEntryLogs(foodEntryLogsRepository.findByUserProfileIdOrderByLoggedAtDesc(userId)
                .stream()
                .map(this::toFoodEntryResponse)
                .toList());

        export.setWeightEntries(weightEntryRepository.findByUserProfileIdOrderByRecordedAtAsc(userId)
                .stream()
                .map(this::toWeightEntryResponse)
                .toList());

        export.setWaterEntries(waterEntryRepository.findByUserProfileIdOrderByLoggedAtAsc(userId)
                .stream()
                .map(this::toWaterEntryResponse)
                .toList());

        export.setWorkoutLogs(workoutLogRepository.findByUserProfileIdOrderByPerformedAtDesc(userId)
                .stream()
                .map(this::toWorkoutLogResponse)
                .toList());

        export.setCompletedWorkouts(completedWorkoutRepository.findByUserProfileIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toCompletedWorkoutResponse)
                .toList());

        export.setNotifications(notificationRepository.findByUserProfileIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toNotificationResponse)
                .toList());

        export.setMessages(messageRepository.findAllForUser(userId)
                .stream()
                .map(this::toMessageDto)
                .toList());

        return export;
    }

    /**
     * Erases the account and returns the id that was deleted, so the caller can revoke the
     * account's tokens once this transaction has actually committed. Revoking in here would
     * survive a rollback — the cache is not transactional — and would lock a still-live
     * account out permanently.
     */
    @Transactional // permanently erase the authenticated user and everything that references them; the whole erasure is one transaction, because a half-deleted account is worse than a failed delete
    public Long deleteCurrentUserAccount() {
        Long userId = currentUserId();

        // Existence is checked without loading the profile, so nothing about the user is
        // held in the persistence context while the bulk deletes below run behind its back.
        if (!userProfileRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found.");
        }

        // No entity declares a cascade onto UserProfile, so every child row has to be
        // removed explicitly and before the parent, or its foreign key rejects the delete.
        // Messages and friend requests reference the user from both sides.
        foodEntryLogsRepository.deleteByUserProfileId(userId);
        weightEntryRepository.deleteByUserProfileId(userId);
        waterEntryRepository.deleteByUserProfileId(userId);
        workoutLogRepository.deleteByUserProfileId(userId);
        completedWorkoutRepository.deleteByUserProfileId(userId);
        notificationRepository.deleteByUserProfileId(userId);
        userGoalsRepository.deleteByUserProfileId(userId);
        messageRepository.deleteAllForUser(userId);
        friendRequestRepository.deleteAllForUser(userId);

        // Removed through the entity rather than a bulk query: the user_profile_aims
        // collection table is only cleaned up by the persistence context.
        userProfileRepository.deleteById(userId);

        return userId;
    }

    private Long currentUserId() { // the two data-subject endpoints take no user id at all, so identity comes from the verified JWT and nowhere else
        Long userId = AuthGuard.currentUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return userId;
    }

    private UserProfileResponse toProfileResponse(UserProfile p) { // deliberately mirrors UserProfileResponse only — the password hash and any other internal auth state stay out of the export
        UserProfileResponse r = new UserProfileResponse();
        r.setId(p.getId());
        r.setEmail(p.getEmail());
        r.setDisplayName(p.getDisplayName());
        r.setAge(p.getAge());
        r.setHeightValue(p.getHeightValue());
        r.setHeightUnit(p.getHeightUnit());
        r.setWeightValue(p.getWeightValue());
        r.setWeightUnit(p.getWeightUnit());
        r.setGender(p.getGender());
        r.setActivityLevel(p.getActivityLevel());
        r.setExperienceLevel(p.getExperienceLevel());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        r.setSelectedWorkoutPlanId(p.getSelectedWorkoutPlanId());
        r.setOnboardingComplete(p.getOnboardingComplete());
        r.setBodyType(p.getBodyType());
        r.setAims(p.getAims());
        r.setProfileVisibility(p.getProfileVisibility());
        r.setShareWeight(p.getShareWeight());
        r.setShareActivity(p.getShareActivity());
        return r;
    }

    private UserGoalsResponse toGoalsResponse(UserGoals g) {
        UserGoalsResponse r = new UserGoalsResponse();
        r.setId(g.getId());
        r.setUserId(g.getUserProfile().getId());
        r.setCalorieGoal(g.getCalorieGoal());
        r.setProteinGoal(g.getProteinGoal());
        r.setCarbGoal(g.getCarbGoal());
        r.setFatGoal(g.getFatGoal());
        r.setCreatedAt(g.getCreatedAt());
        r.setUpdatedAt(g.getUpdatedAt());
        return r;
    }

    private FoodEntryLogsResponse toFoodEntryResponse(FoodEntryLogs e) {
        FoodEntryLogsResponse r = new FoodEntryLogsResponse();
        r.setId(e.getId());
        r.setUserId(e.getUserProfile().getId());
        r.setFoodName(e.getFoodName());
        r.setWeightValue(e.getWeightValue());
        r.setWeightUnit(e.getWeightUnit());
        r.setCalories(e.getCalories());
        r.setProteins(e.getProteins());
        r.setCarbs(e.getCarbs());
        r.setFats(e.getFats());
        r.setFiberG(e.getFiberG());
        r.setSugarG(e.getSugarG());
        r.setSodiumMg(e.getSodiumMg());
        r.setPotassiumMg(e.getPotassiumMg());
        r.setCholesterolMg(e.getCholesterolMg());
        r.setSaturatedFatG(e.getSaturatedFatG());
        r.setVitaminAMcg(e.getVitaminAMcg());
        r.setVitaminCMg(e.getVitaminCMg());
        r.setVitaminDMcg(e.getVitaminDMcg());
        r.setCalciumMg(e.getCalciumMg());
        r.setIronMg(e.getIronMg());
        r.setZincMg(e.getZincMg());
        r.setMealType(e.getMealType());
        r.setLoggedAt(e.getLoggedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    private WeightEntryResponse toWeightEntryResponse(WeightEntry e) {
        WeightEntryResponse r = new WeightEntryResponse();
        r.setId(e.getId());
        r.setUserId(e.getUserProfile().getId());
        r.setWeightValue(e.getWeightValue());
        r.setWeightUnit(e.getWeightUnit());
        r.setRecordedAt(e.getRecordedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setBodyFatPercent(e.getBodyFatPercent());
        r.setProteinPercent(e.getProteinPercent());
        r.setMuscleMassKg(e.getMuscleMassKg());
        r.setVisceralFatLevel(e.getVisceralFatLevel());
        r.setBmi(e.getBmi());
        r.setBoneMassKg(e.getBoneMassKg());
        r.setWaterPercent(e.getWaterPercent());
        r.setBmr(e.getBmr());
        r.setWaistCm(e.getWaistCm());
        r.setHipCm(e.getHipCm());
        r.setChestCm(e.getChestCm());
        r.setNeckCm(e.getNeckCm());
        r.setShoulderCm(e.getShoulderCm());
        r.setLeftBicepCm(e.getLeftBicepCm());
        r.setRightBicepCm(e.getRightBicepCm());
        r.setLeftForearmCm(e.getLeftForearmCm());
        r.setRightForearmCm(e.getRightForearmCm());
        r.setAbdomenCm(e.getAbdomenCm());
        r.setLeftThighCm(e.getLeftThighCm());
        r.setRightThighCm(e.getRightThighCm());
        r.setLeftCalfCm(e.getLeftCalfCm());
        r.setRightCalfCm(e.getRightCalfCm());
        r.setSource(e.getSource());
        return r;
    }

    private WaterEntryResponse toWaterEntryResponse(WaterEntry e) {
        WaterEntryResponse r = new WaterEntryResponse();
        r.setId(e.getId());
        r.setUserId(e.getUserProfile().getId());
        r.setWaterMl(e.getWaterMl());
        r.setLoggedAt(e.getLoggedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    private WorkoutLogResponse toWorkoutLogResponse(WorkoutLog w) {
        WorkoutLogResponse r = new WorkoutLogResponse();
        r.setId(w.getId());
        r.setUserId(w.getUserProfile().getId());
        r.setWorkoutName(w.getWorkoutName());
        r.setWorkoutType(w.getWorkoutType());
        r.setDurationMinutes(w.getDurationMinutes());
        r.setPerformedAt(w.getPerformedAt());
        r.setNotes(w.getNotes());
        r.setDetailsJson(w.getDetailsJson());
        r.setCreatedAt(w.getCreatedAt());
        r.setUpdatedAt(w.getUpdatedAt());
        return r;
    }

    private CompletedWorkoutResponse toCompletedWorkoutResponse(CompletedWorkout cw) {
        CompletedWorkoutResponse r = new CompletedWorkoutResponse();
        r.setId(cw.getId());
        r.setUserId(cw.getUserProfile().getId());
        r.setWorkoutPlanId(cw.getWorkoutPlan().getId());
        r.setWorkoutPlanTitle(cw.getWorkoutPlan().getTitle());
        r.setCompletedAt(cw.getCompletedAt());
        r.setDurationMinutes(cw.getDurationMinutes());
        r.setNotes(cw.getNotes());
        return r;
    }

    private NotificationResponse toNotificationResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setUserId(n.getUserProfile().getId());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setNotificationType(n.getNotificationType());
        r.setIsRead(n.getIsRead());
        r.setScheduledFor(n.getScheduledFor());
        r.setCreatedAt(n.getCreatedAt());
        r.setReadAt(n.getReadAt());
        return r;
    }

    private MessageDto toMessageDto(Message m) {
        MessageDto d = new MessageDto();
        d.setId(m.getId());
        d.setSenderId(m.getSender().getId());
        d.setSenderName(m.getSender().getDisplayName());
        d.setReceiverId(m.getReceiver().getId());
        d.setReceiverName(m.getReceiver().getDisplayName());
        d.setContent(m.getContent());
        d.setSentAt(m.getSentAt());
        d.setReadAt(m.getReadAt());
        return d;
    }
}
