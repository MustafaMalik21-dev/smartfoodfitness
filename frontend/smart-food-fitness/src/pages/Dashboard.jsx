import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeaderBar from "../components/HeaderBar";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import "./Dashboard.css";
import StreakIcon from "../assets/Streakicon.png";
import { getUserId } from "../auth/authStorage";



function clamp(n, min, max) { // clamp number between min and max
  return Math.max(min, Math.min(max, n));
}

function toNumber(v) { // convert value to number or 0
  const n = Number(v); 
  return Number.isFinite(n) ? n : 0; // return 0 if not a finite number
}

function percentFromCurrentGoal(current, goal) { // calculate percentage from current and goal values
  if (!goal || goal <= 0) return 0;
  return clamp((current / goal) * 100, 0, 100); // clamp between 0 and 100
}

function formatInt(n) {
  return Math.round(toNumber(n)).toString(); // format number as integer string
}

function formatMacroLine(name, current, goal) { // format macro nutrient line
  return `${name} - ${formatInt(current)}g / ${formatInt(goal)}g`; // e.g., "Protein - 120g / 150g"
}

function Donut({ percent, size = 160, stroke = 18, labelLeft, labelRight }) { // Donut chart component
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const p = clamp(toNumber(percent), 0, 100); 
  const offset = c * (1 - p / 100); // calculate stroke offset for percentage

  return (
    <div className="donutWrap" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="donutSvg">
        <circle cx={size / 2} cy={size / 2} r={r} className="donutTrack" strokeWidth={stroke} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          className="donutFill"
          strokeWidth={stroke}
          strokeDasharray={c}
          strokeDashoffset={offset}
        />
      </svg>

      <div className="donutLabels">
        <div className="donutLeft">
          <div className="donutPct">{Math.round(p)}%</div>
          <div className="donutSmall">Consumed</div>
        </div>
        <div className="donutRight">
          <div className="donutPct">{Math.round(100 - p)}%</div>
          <div className="donutSmall">Remaining</div>
        </div>
      </div>

      <div className="donutBottomLabels">
        <span className="donutBottomLeft">{labelLeft}</span>
        <span className="donutBottomRight">{labelRight}</span>
      </div>
    </div>
  );
}

function MiniRing({ current, goal, colorVar = "--primary" }) { // Mini progress ring component
  const p = percentFromCurrentGoal(current, goal); // calculate percentage
  const size = 60;
  const stroke = 10;
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const offset = c * (1 - p / 100);

  return (
    <div className="miniRing">
      <svg width={size} height={size} className="miniRingSvg">
        <circle cx={size / 2} cy={size / 2} r={r} className="miniTrack" strokeWidth={stroke} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          className="miniFill"
          strokeWidth={stroke}
          strokeDasharray={c}
          strokeDashoffset={offset}
          style={{ stroke: `var(${colorVar})` }}
        />
      </svg>
    </div>
  );
}


export default function Dashboard() { // Dashboard page
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  const userId = getUserId();

  useEffect(() => { // data loading effect
    let cancelled = false; 
    if (!userId) return;

    async function load() { // load dashboard data
      try {
        setLoading(true);
        const res = await apiClient.get(`/api/dashboard-summary/user/${userId}`, {
          params: { timezone: "Europe/London" },
        });
        if (!cancelled) setDashboard(res.data);
      } catch {
        if (!cancelled) setDashboard(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();

    const poll = setInterval(load, 15000); // poll every 15 seconds

    const onFocus = () => load(); 
    const onVisibility = () => {
      if (document.visibilityState === "visible") load(); 
    }; 

    window.addEventListener("focus", onFocus); // add focus event listener
    document.addEventListener("visibilitychange", onVisibility); // add visibility change listener

    return () => { // cleanup on unmount
      cancelled = true;
      clearInterval(poll);
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [userId]);

  const view = useMemo(() => { // prepare view data
    const d = dashboard ?? {};

    const caloriesGoal = toNumber(d.caloriesGoal); // daily calorie goal
    const caloriesTotal = toNumber(d.totalCalories);
    const caloriesRemaining = toNumber(d.caloriesRemaining);
    const caloriesPercent =
      toNumber(d.caloriesPercent) > 0
        ? toNumber(d.caloriesPercent)   // use provided percent if valid
        : percentFromCurrentGoal(caloriesTotal, caloriesGoal); // calculate percent consumed

    const proteinsGoal = toNumber(d.proteinsGoal);
    const proteinsTotal = toNumber(d.totalProteins);

    const carbsGoal = toNumber(d.carbsGoal);
    const carbsTotal = toNumber(d.totalCarbs);

    const fatsGoal = toNumber(d.fatsGoal);
    const fatsTotal = toNumber(d.totalFats);

    const currentStreakDays = toNumber(d.currentStreakDays);
    const workoutsLast7Days = toNumber(d.workoutsLast7Days);
    const totalWorkouts = toNumber(d.totalWorkouts);

    const macroRows = [
      { label: "Protein", current: proteinsTotal, goal: proteinsGoal, colorVar: "--dashProtein" },
      { label: "Carbs", current: carbsTotal, goal: carbsGoal, colorVar: "--dashCarbs" },
      { label: "Fat", current: fatsTotal, goal: fatsGoal, colorVar: "--dashFat" },
    ];

    return {
      caloriesGoal,
      caloriesTotal,
      caloriesRemaining,
      caloriesPercent,
      macroRows,
      currentStreakDays,
      workoutsLast7Days,
      totalWorkouts,
    };
  }, [dashboard]);

  return (
    <div className="pageShell">
      <HeaderBar title="Dashboard" left="profile" right="notifications" />

      <div className="pageBody">
        <div className="streakPill">
          <span className="streakIcon" aria-hidden="true">
            <img className="streakIconImg" src={StreakIcon} alt="" />
          </span>
          <span className="streakText">{loading ? "…" : `${view.currentStreakDays} Day Streak`}</span>
        </div>

        <div className="calorieCard">
          <div className="calorieCardInner">
            <div className="macroLeft">
              <div className="macroLine">
                <MiniRing
                  current={view.macroRows[0].current}
                  goal={view.macroRows[0].goal}
                  colorVar={view.macroRows[0].colorVar}
                />
                <div className="macroText">
                  {formatMacroLine("Protein", view.macroRows[0].current, view.macroRows[0].goal)}
                </div>
              </div>

              <div className="macroLine">
                <MiniRing
                  current={view.macroRows[1].current}
                  goal={view.macroRows[1].goal}
                  colorVar={view.macroRows[1].colorVar}
                />
                <div className="macroText">
                  {formatMacroLine("Carbs", view.macroRows[1].current, view.macroRows[1].goal)}
                </div>
              </div>

              <div className="macroLine">
                <MiniRing
                  current={view.macroRows[2].current}
                  goal={view.macroRows[2].goal}
                  colorVar={view.macroRows[2].colorVar}
                />
                <div className="macroText">
                  {formatMacroLine("Fat", view.macroRows[2].current, view.macroRows[2].goal)}
                </div>
              </div>
            </div>

            <div className="calorieRight">
              <div className="calorieHeader">
                <div className="calGoalLabel">Calorie Goal</div>
                <div className="calGoalValue">{formatInt(view.caloriesGoal)} kcal</div>
              </div>

              <div className="calRemaining">
                <span className="calRemainingLabel">Calories Remaining :</span>
                <span className="calRemainingValue"> {formatInt(view.caloriesRemaining)} kcal</span>
              </div>

              <div className="donutTitle">Daily Calorie Goal Progress</div>

              <Donut percent={view.caloriesPercent} labelLeft="Consumed" labelRight="Remaining" />
            </div>
          </div>
        </div>

        <div className="quickActions">
          <button className="pillBtn" type="button" onClick={() => navigate("/food/log")}>
            Log Food
          </button>
          <button className="pillBtn" type="button" onClick={() => navigate("/workout")}>
            Start Workout
          </button>
          <button className="pillBtn" type="button" onClick={() => navigate("/tracking")}>
            View Progress
          </button>
        </div>

        <div className="tipCard">
          <div className="tipLabel">Daily Tip:</div>
          <div className="tipText">
            {loading ? "" : "Try to hit your protein goal early in the day to stay consistent."}
          </div>
        </div>

        <div className="dashStatsTiny">
          <div className="tinyStat">Total workouts: {formatInt(view.totalWorkouts)}</div>
          <div className="tinyStat">Last 7 days: {formatInt(view.workoutsLast7Days)}</div>
        </div>
      </div>
    </div>
  );
}
