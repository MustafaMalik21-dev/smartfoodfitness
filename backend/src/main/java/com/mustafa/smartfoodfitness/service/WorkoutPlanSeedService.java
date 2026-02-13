package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanSessionRepository;

@Service
public class WorkoutPlanSeedService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutPlanSessionRepository sessionRepository;

    public WorkoutPlanSeedService(
            WorkoutPlanRepository workoutPlanRepository,
            WorkoutPlanSessionRepository sessionRepository
    ) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.sessionRepository = sessionRepository;
    }

    public String seedIfEmpty() {
        if (!workoutPlanRepository.findByIsActiveTrueOrderByTitleAsc().isEmpty()) {
            return "Active plans already exist.";
        }

        Instant now = Instant.now();
        List<WorkoutPlan> plans = new ArrayList<>();

        final String SPLIT_UL = "Upper/Lower";
        final String SPLIT_PPL = "Push/Pull/Legs";
        final String SPLIT_FB = "Full Body";
        final String SPLIT_BRO = "Bro Split";
        final String SPLIT_ARMS = "Arms Focus";
        final String SPLIT_LEGS = "Legs Focus";
        final String SPLIT_SA = "Shoulders & Arms";

        plans.add(makePlan(now, "Full Body Starter (3 Days)", "Beginner", "General Fitness", SPLIT_FB, 3, 45,
                "Simple full body plan to build consistency.", "Easy to follow; balanced; great habit builder.", "Less specialised."));
        plans.add(makePlan(now, "Upper/Lower Starter (4 Days)", "Beginner", "Strength", SPLIT_UL, 4, 50,
                "Upper/lower split focusing on technique and progression.", "Great structure; good frequency.", "Requires consistency."));
        plans.add(makePlan(now, "Upper/Lower Beginner Hypertrophy", "Beginner", "Muscle Gain", SPLIT_UL, 4, 50,
                "Hypertrophy-friendly upper/lower with simple progression.", "Solid volume; easy exercise selection.", "Can feel repetitive."));

        plans.add(makePlan(now, "Classic PPL (5 Days)", "Intermediate", "Muscle Gain", SPLIT_PPL, 5, 60,
                "Hypertrophy focused push/pull/legs.", "Great volume and variety.", "Needs good recovery."));
        plans.add(makePlan(now, "Full Body Strength (3 Days)", "Intermediate", "Strength", SPLIT_FB, 3, 60,
                "Compound-heavy full body with progression.", "Efficient; strong strength gains.", "Can be fatiguing."));
        plans.add(makePlan(now, "Arms Focus (4 Days)", "Intermediate", "Muscle Gain", SPLIT_ARMS, 4, 55,
                "Extra arms volume with upper support work.", "Big arms emphasis.", "Other muscles are maintenance volume."));
        plans.add(makePlan(now, "Shoulders & Arms (4 Days)", "Intermediate", "Muscle Gain", SPLIT_SA, 4, 55,
                "Upper focus with shoulders/arms emphasis.", "Great delts/arms growth.", "Lower is minimal."));
        plans.add(makePlan(now, "Legs Focus (4 Days)", "Intermediate", "Strength", SPLIT_LEGS, 4, 55,
                "Lower-focused strength with core and posterior chain work.", "Strong lower body gains.", "Upper is maintenance."));

        plans.add(makePlan(now, "PPL High Frequency (6 Days)", "Advanced", "Muscle Gain", SPLIT_PPL, 6, 60,
                "High frequency hypertrophy with high weekly volume.", "Fast progression; lots of work.", "Recovery demand is high."));
        plans.add(makePlan(now, "Bro Split (5 Days)", "Advanced", "Muscle Gain", SPLIT_BRO, 5, 65,
                "Body-part split with high focus per day.", "Great pump/volume per muscle.", "Each muscle hit less frequently."));
        plans.add(makePlan(now, "Fat Loss Conditioning + Weights (5 Days)", "Advanced", "Fat Loss", SPLIT_PPL, 5, 50,
                "Weights with conditioning finishers to support fat loss.", "High calorie burn; maintains strength stimulus.", "Recovery demand is high."));

        workoutPlanRepository.saveAll(plans);

        for (WorkoutPlan p : plans) {
            seedSessionsForPlan(now, p);
        }

        return "Seeded " + plans.size() + " workout plans + sessions.";
    }

    private void seedSessionsForPlan(Instant now, WorkoutPlan plan) {
        String split = plan.getSplit() == null ? "" : plan.getSplit().trim();
        List<WorkoutPlanSession> sessions = new ArrayList<>();

        if (split.equalsIgnoreCase("Upper/Lower")) {
            sessions.add(makeSession(now, plan, 0, "Upper Day", "Upper", jsonUpper(), 50));
            sessions.add(makeSession(now, plan, 1, "Lower Day", "Lower", jsonLower(), 50));
            sessions.add(makeSession(now, plan, 2, "Upper Day (B)", "Upper", jsonUpperB(), 50));
            sessions.add(makeSession(now, plan, 3, "Lower Day (B)", "Lower", jsonLowerB(), 50));

        } else if (split.equalsIgnoreCase("Push/Pull/Legs")) {
            sessions.add(makeSession(now, plan, 0, "Push Day", "Upper", jsonPush(), 55));
            sessions.add(makeSession(now, plan, 1, "Pull Day", "Upper", jsonPull(), 55));
            sessions.add(makeSession(now, plan, 2, "Leg Day", "Lower", jsonLegs(), 55));

        } else if (split.equalsIgnoreCase("Full Body")) {
            sessions.add(makeSession(now, plan, 0, "Full Body A", "Full Body", jsonFullA(), 45));
            sessions.add(makeSession(now, plan, 1, "Full Body B", "Full Body", jsonFullB(), 45));
            sessions.add(makeSession(now, plan, 2, "Full Body C", "Full Body", jsonFullC(), 45));

        } else if (split.equalsIgnoreCase("Bro Split")) {
            sessions.add(makeSession(now, plan, 0, "Chest Day", "Upper", jsonChestDay(), 60));
            sessions.add(makeSession(now, plan, 1, "Back Day", "Upper", jsonBackDay(), 60));
            sessions.add(makeSession(now, plan, 2, "Leg Day", "Lower", jsonLegDayBro(), 65));
            sessions.add(makeSession(now, plan, 3, "Shoulders Day", "Upper", jsonShouldersDay(), 55));
            sessions.add(makeSession(now, plan, 4, "Arms Day", "Upper", jsonArmsDay(), 55));

        } else if (split.equalsIgnoreCase("Arms Focus")) {
            sessions.add(makeSession(now, plan, 0, "Arms Day", "Upper", jsonArmsDay(), 55));
            sessions.add(makeSession(now, plan, 1, "Upper Support", "Upper", jsonUpperSupport(), 55));
            sessions.add(makeSession(now, plan, 2, "Lower Maintenance", "Lower", jsonLowerMaintenance(), 45));
            sessions.add(makeSession(now, plan, 3, "Arms Day (B)", "Upper", jsonArmsDayB(), 55));

        } else if (split.equalsIgnoreCase("Legs Focus")) {
            sessions.add(makeSession(now, plan, 0, "Leg Day", "Lower", jsonLegDayBro(), 60));
            sessions.add(makeSession(now, plan, 1, "Posterior Chain", "Lower", jsonPosteriorChain(), 60));
            sessions.add(makeSession(now, plan, 2, "Upper Maintenance", "Upper", jsonUpperMaintenance(), 45));
            sessions.add(makeSession(now, plan, 3, "Core + Conditioning", "Lower", jsonCoreConditioning(), 40));

        } else if (split.equalsIgnoreCase("Shoulders & Arms")) {
            sessions.add(makeSession(now, plan, 0, "Shoulders Day", "Upper", jsonShouldersDay(), 55));
            sessions.add(makeSession(now, plan, 1, "Arms Day", "Upper", jsonArmsDay(), 55));
            sessions.add(makeSession(now, plan, 2, "Upper (Chest/Back)", "Upper", jsonUpperChestBack(), 55));
            sessions.add(makeSession(now, plan, 3, "Lower Maintenance", "Lower", jsonLowerMaintenance(), 45));
        }

        if (!sessions.isEmpty()) {
            sessionRepository.saveAll(sessions);
        }
    }

    private WorkoutPlan makePlan(
            Instant now,
            String title,
            String level,
            String goal,
            String split,
            Integer daysPerWeek,
            Integer estMins,
            String desc,
            String pros,
            String cons
    ) {
        WorkoutPlan p = new WorkoutPlan();
        p.setTitle(title);
        p.setLevel(level);
        p.setGoal(goal);
        p.setSplit(split);
        p.setDaysPerWeek(daysPerWeek);
        p.setEstimatedDurationMinutes(estMins);
        p.setShortDescription(desc);
        p.setPros(pros);
        p.setCons(cons);
        p.setIsActive(true);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return p;
    }

    private WorkoutPlanSession makeSession(
            Instant now,
            WorkoutPlan plan,
            int idx,
            String title,
            String focus,
            String exerciseJson,
            Integer mins
    ) {
        WorkoutPlanSession s = new WorkoutPlanSession();
        s.setWorkoutPlan(plan);
        s.setSessionIndex(idx);
        s.setTitle(title);
        s.setFocus(focus);
        s.setExerciseJson(exerciseJson);
        s.setEstimatedMinutes(mins);
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        return s;
    }

    private String jsonUpper() { return """
    [
      {"name":"Bench Press","sets":3,"reps":8,"notes":"Controlled reps"},
      {"name":"Incline Dumbbell Press","sets":3,"reps":10,"notes":""},
      {"name":"Seated Row","sets":3,"reps":10,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Lateral Raise","sets":3,"reps":12,"notes":""},
      {"name":"Tricep Pushdown","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonUpperB() { return """
    [
      {"name":"Dumbbell Bench Press","sets":3,"reps":10,"notes":""},
      {"name":"Chest Fly","sets":3,"reps":12,"notes":""},
      {"name":"One-Arm Row","sets":3,"reps":10,"notes":""},
      {"name":"Face Pull","sets":3,"reps":12,"notes":""},
      {"name":"Bicep Curl","sets":3,"reps":12,"notes":""},
      {"name":"Overhead Tricep Extension","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonLower() { return """
    [
      {"name":"Squat","sets":3,"reps":8,"notes":"Depth + control"},
      {"name":"Romanian Deadlift","sets":3,"reps":10,"notes":""},
      {"name":"Leg Press","sets":3,"reps":10,"notes":""},
      {"name":"Hamstring Curl","sets":3,"reps":12,"notes":""},
      {"name":"Calf Raise","sets":3,"reps":12,"notes":""},
      {"name":"Plank","sets":3,"reps":45,"notes":"seconds"}
    ]
    """; }

    private String jsonLowerB() { return """
    [
      {"name":"Deadlift","sets":3,"reps":5,"notes":"Moderate weight"},
      {"name":"Lunge","sets":3,"reps":10,"notes":"each leg"},
      {"name":"Leg Extension","sets":3,"reps":12,"notes":""},
      {"name":"Hip Thrust","sets":3,"reps":10,"notes":""},
      {"name":"Calf Raise","sets":3,"reps":12,"notes":""},
      {"name":"Hanging Knee Raise","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonPush() { return """
    [
      {"name":"Bench Press","sets":4,"reps":6,"notes":"Strength focus"},
      {"name":"Overhead Press","sets":3,"reps":8,"notes":""},
      {"name":"Incline Press","sets":3,"reps":10,"notes":""},
      {"name":"Lateral Raise","sets":3,"reps":12,"notes":""},
      {"name":"Tricep Dip","sets":3,"reps":10,"notes":""}
    ]
    """; }

    private String jsonPull() { return """
    [
      {"name":"Pull-Up / Assisted Pull-Up","sets":4,"reps":6,"notes":""},
      {"name":"Barbell Row","sets":3,"reps":8,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Face Pull","sets":3,"reps":12,"notes":""},
      {"name":"Bicep Curl","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonLegs() { return """
    [
      {"name":"Squat","sets":4,"reps":6,"notes":""},
      {"name":"Romanian Deadlift","sets":3,"reps":8,"notes":""},
      {"name":"Leg Press","sets":3,"reps":10,"notes":""},
      {"name":"Hamstring Curl","sets":3,"reps":12,"notes":""},
      {"name":"Calf Raise","sets":4,"reps":12,"notes":""},
      {"name":"Plank","sets":3,"reps":45,"notes":"seconds"}
    ]
    """; }

    private String jsonFullA() { return """
    [
      {"name":"Squat","sets":3,"reps":8,"notes":""},
      {"name":"Bench Press","sets":3,"reps":8,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Romanian Deadlift","sets":3,"reps":10,"notes":""},
      {"name":"Plank","sets":3,"reps":45,"notes":"seconds"}
    ]
    """; }

    private String jsonFullB() { return """
    [
      {"name":"Leg Press","sets":3,"reps":10,"notes":""},
      {"name":"Incline Dumbbell Press","sets":3,"reps":10,"notes":""},
      {"name":"Seated Row","sets":3,"reps":10,"notes":""},
      {"name":"Lateral Raise","sets":3,"reps":12,"notes":""},
      {"name":"Hanging Knee Raise","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonFullC() { return """
    [
      {"name":"Deadlift","sets":3,"reps":5,"notes":""},
      {"name":"Dumbbell Bench Press","sets":3,"reps":10,"notes":""},
      {"name":"One-Arm Row","sets":3,"reps":10,"notes":""},
      {"name":"Calf Raise","sets":3,"reps":12,"notes":""},
      {"name":"Crunch","sets":3,"reps":15,"notes":""}
    ]
    """; }

    private String jsonChestDay() { return """
    [
      {"name":"Bench Press","sets":4,"reps":6,"notes":""},
      {"name":"Incline Dumbbell Press","sets":3,"reps":10,"notes":""},
      {"name":"Chest Fly","sets":3,"reps":12,"notes":""},
      {"name":"Dips","sets":3,"reps":10,"notes":""},
      {"name":"Tricep Pushdown","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonBackDay() { return """
    [
      {"name":"Pull-Up / Assisted Pull-Up","sets":4,"reps":6,"notes":""},
      {"name":"Barbell Row","sets":3,"reps":8,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Seated Row","sets":3,"reps":10,"notes":""},
      {"name":"Face Pull","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonLegDayBro() { return """
    [
      {"name":"Squat","sets":4,"reps":6,"notes":""},
      {"name":"Romanian Deadlift","sets":3,"reps":8,"notes":""},
      {"name":"Leg Press","sets":3,"reps":10,"notes":""},
      {"name":"Leg Extension","sets":3,"reps":12,"notes":""},
      {"name":"Hamstring Curl","sets":3,"reps":12,"notes":""},
      {"name":"Calf Raise","sets":4,"reps":12,"notes":""}
    ]
    """; }

    private String jsonShouldersDay() { return """
    [
      {"name":"Overhead Press","sets":4,"reps":6,"notes":""},
      {"name":"Lateral Raise","sets":4,"reps":12,"notes":""},
      {"name":"Rear Delt Fly","sets":3,"reps":12,"notes":""},
      {"name":"Face Pull","sets":3,"reps":12,"notes":""},
      {"name":"Shrugs","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonArmsDay() { return """
    [
      {"name":"Bicep Curl","sets":4,"reps":12,"notes":""},
      {"name":"Hammer Curl","sets":3,"reps":12,"notes":""},
      {"name":"Tricep Pushdown","sets":4,"reps":12,"notes":""},
      {"name":"Overhead Tricep Extension","sets":3,"reps":12,"notes":""},
      {"name":"Cable Curl","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonArmsDayB() { return """
    [
      {"name":"EZ-Bar Curl","sets":4,"reps":10,"notes":""},
      {"name":"Incline Dumbbell Curl","sets":3,"reps":12,"notes":""},
      {"name":"Close-Grip Bench Press","sets":4,"reps":8,"notes":""},
      {"name":"Skull Crushers","sets":3,"reps":10,"notes":""},
      {"name":"Rope Pushdown","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonUpperSupport() { return """
    [
      {"name":"Incline Press","sets":3,"reps":10,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Seated Row","sets":3,"reps":10,"notes":""},
      {"name":"Lateral Raise","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonUpperMaintenance() { return """
    [
      {"name":"Bench Press","sets":3,"reps":8,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Lateral Raise","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonLowerMaintenance() { return """
    [
      {"name":"Leg Press","sets":3,"reps":10,"notes":""},
      {"name":"Hamstring Curl","sets":3,"reps":12,"notes":""},
      {"name":"Calf Raise","sets":3,"reps":12,"notes":""},
      {"name":"Plank","sets":3,"reps":45,"notes":"seconds"}
    ]
    """; }

    private String jsonPosteriorChain() { return """
    [
      {"name":"Deadlift","sets":3,"reps":5,"notes":""},
      {"name":"Hip Thrust","sets":3,"reps":10,"notes":""},
      {"name":"Romanian Deadlift","sets":3,"reps":8,"notes":""},
      {"name":"Hamstring Curl","sets":3,"reps":12,"notes":""},
      {"name":"Back Extension","sets":3,"reps":12,"notes":""}
    ]
    """; }

    private String jsonCoreConditioning() { return """
    [
      {"name":"Plank","sets":3,"reps":45,"notes":"seconds"},
      {"name":"Hanging Knee Raise","sets":3,"reps":12,"notes":""},
      {"name":"Bicycle Crunch","sets":3,"reps":20,"notes":"total"},
      {"name":"Treadmill Intervals","sets":8,"reps":30,"notes":"seconds run / 60s walk"}
    ]
    """; }

    private String jsonUpperChestBack() { return """
    [
      {"name":"Bench Press","sets":3,"reps":8,"notes":""},
      {"name":"Incline Dumbbell Press","sets":3,"reps":10,"notes":""},
      {"name":"Lat Pulldown","sets":3,"reps":10,"notes":""},
      {"name":"Seated Row","sets":3,"reps":10,"notes":""}
    ]
    """; }
}
