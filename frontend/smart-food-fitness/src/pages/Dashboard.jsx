import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeaderBar from "../components/HeaderBar";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import "./Dashboard.css";
import StreakIcon from "../assets/Streakicon.png";
import { getUserId } from "../auth/authStorage";

function clamp(n, min, max) {
  return Math.max(min, Math.min(max, n));
}

function toNumber(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function percentFromCurrentGoal(current, goal) {
  const c = Math.max(0, toNumber(current));
  const g = Math.max(0, toNumber(goal));
  if (!g) return 0;
  return clamp((c / g) * 100, 0, 100);
}

function formatInt(n) {
  return Math.round(toNumber(n)).toString();
}

function capCurrentToGoal(current, goal) {
  const c = Math.max(0, toNumber(current));
  const g = Math.max(0, toNumber(goal));
  if (g > 0) return Math.min(c, g);
  return c;
}

function formatMacroLine(name, current, goal) {
  const displayCurrent = capCurrentToGoal(current, goal);
  return `${name} - ${formatInt(displayCurrent)}g / ${formatInt(Math.max(0, toNumber(goal)))}g`;
}

function Donut({ percent, size = 160, stroke = 18, labelLeft, labelRight }) {
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const p = clamp(toNumber(percent), 0, 100);
  const offset = c * (1 - p / 100);

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

function MiniRing({ current, goal, colorVar = "--primary" }) {
  const p = percentFromCurrentGoal(current, goal);
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

export default function Dashboard() {
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  const userId = getUserId();

  useEffect(() => {
    let cancelled = false;
    if (!userId) return;

    async function load() {
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

    const poll = setInterval(load, 15000);

    const onFocus = () => load();
    const onVisibility = () => {
      if (document.visibilityState === "visible") load();
    };

    window.addEventListener("focus", onFocus);
    document.addEventListener("visibilitychange", onVisibility);

    return () => {
      cancelled = true;
      clearInterval(poll);
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [userId]);

  const view = useMemo(() => {
    const d = dashboard ?? {};

    const caloriesGoal = Math.max(0, toNumber(d.caloriesGoal));
    const caloriesTotal = Math.max(0, toNumber(d.totalCalories));

    const remainingRaw = caloriesGoal - caloriesTotal;
    const caloriesRemaining = Math.max(0, remainingRaw);
    const overBy = Math.max(0, -remainingRaw);
    const isOver = overBy > 0;

    const caloriesPercent = caloriesGoal > 0 ? clamp((caloriesTotal / caloriesGoal) * 100, 0, 100) : 0;

    const proteinsGoal = Math.max(0, toNumber(d.proteinsGoal));
    const proteinsTotal = Math.max(0, toNumber(d.totalProteins));

    const carbsGoal = Math.max(0, toNumber(d.carbsGoal));
    const carbsTotal = Math.max(0, toNumber(d.totalCarbs));

    const fatsGoal = Math.max(0, toNumber(d.fatsGoal));
    const fatsTotal = Math.max(0, toNumber(d.totalFats));

    const currentStreakDays = Math.max(0, toNumber(d.currentStreakDays));
    const workoutsLast7Days = Math.max(0, toNumber(d.workoutsLast7Days));
    const totalWorkouts = Math.max(0, toNumber(d.totalWorkouts));

    const macroRows = [
      { label: "Protein", current: proteinsTotal, goal: proteinsGoal, colorVar: "--dashProtein" },
      { label: "Carbs", current: carbsTotal, goal: carbsGoal, colorVar: "--dashCarbs" },
      { label: "Fat", current: fatsTotal, goal: fatsGoal, colorVar: "--dashFat" },
    ];

    return {
      caloriesGoal,
      caloriesTotal,
      caloriesRemaining,
      caloriesPercent: clamp(caloriesPercent, 0, 100),
      isOver,
      overBy,
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
                <MiniRing current={view.macroRows[0].current} goal={view.macroRows[0].goal} colorVar={view.macroRows[0].colorVar} />
                <div className="macroText">{formatMacroLine("Protein", view.macroRows[0].current, view.macroRows[0].goal)}</div>
              </div>

              <div className="macroLine">
                <MiniRing current={view.macroRows[1].current} goal={view.macroRows[1].goal} colorVar={view.macroRows[1].colorVar} />
                <div className="macroText">{formatMacroLine("Carbs", view.macroRows[1].current, view.macroRows[1].goal)}</div>
              </div>

              <div className="macroLine">
                <MiniRing current={view.macroRows[2].current} goal={view.macroRows[2].goal} colorVar={view.macroRows[2].colorVar} />
                <div className="macroText">{formatMacroLine("Fat", view.macroRows[2].current, view.macroRows[2].goal)}</div>
              </div>
            </div>

            <div className="calorieRight">
              <div className="calorieHeader">
                <div className="calGoalLabel">Calorie Goal</div>
                <div className="calGoalValue">{formatInt(view.caloriesGoal)} kcal</div>
              </div>

              <div className="calRemaining">
                <span className="calRemainingLabel">{view.isOver ? "Over by :" : "Calories Remaining :"}</span>
                <span className="calRemainingValue"> {view.isOver ? formatInt(view.overBy) : formatInt(view.caloriesRemaining)} kcal</span>
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
          <div className="tipText">{loading ? "" : "Try to hit your protein goal early in the day to stay consistent."}</div>
        </div>

        <div className="dashStatsTiny">
          <div className="tinyStat">Total workouts: {formatInt(view.totalWorkouts)}</div>
          <div className="tinyStat">Last 7 days: {formatInt(view.workoutsLast7Days)}</div>
        </div>
      </div>
    </div>
  );
}
