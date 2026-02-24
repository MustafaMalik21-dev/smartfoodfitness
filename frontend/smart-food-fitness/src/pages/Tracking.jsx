import { useEffect, useMemo, useState } from "react";
import HeaderBar from "../components/HeaderBar";
import apiClient from "../api/apiClient";
import "./Tracking.css";
import { getUserId } from "../auth/authStorage";

import { // Importing necessary components and libraries for the Tracking page, including React hooks for state and effect management, a header component for consistent page layout, an API client for fetching data from the backend, CSS for styling, and a function to retrieve the current user's ID from authentication storage. Additionally, importing Chart.js components and the Line chart component from react-chartjs-2 for rendering the tracking charts on the page.
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Filler,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Filler);

const SETTINGS_KEY = "sff_settings_v1";

const DEFAULT_SETTINGS = { // Default settings for the tracking page, including units for weight and height, tracking preferences for macros, weight, and workouts, notification preferences for workouts, food, and streaks, and accessibility options for text size and mode. These settings provide a baseline configuration that can be customized by the user and persisted in localStorage to maintain user preferences across sessions.
  units: { weight: "kg", height: "ft" },
  tracking: { macros: true, weight: true, workouts: true },
  notifications: { workouts: true, food: true, streak: true },
  accessibility: { textSize: "small", mode: "light" },
};

const RANGE_BY_TAB = { // mapping of tab to API range param
  daily: "week",
  weekly: "month",
  monthly: "year",
};

const WEEKLY_TARGET = 5;
const MONTHLY_TARGET = 20;

function safeParse(json) {
  try {
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function mergeDefaults(saved) { //Merge saved settings with defaults
  if (!saved || typeof saved !== "object") return DEFAULT_SETTINGS;
  return {
    units: { ...DEFAULT_SETTINGS.units, ...(saved.units || {}) },
    tracking: { ...DEFAULT_SETTINGS.tracking, ...(saved.tracking || {}) },
    notifications: { ...DEFAULT_SETTINGS.notifications, ...(saved.notifications || {}) },
    accessibility: { ...DEFAULT_SETTINGS.accessibility, ...(saved.accessibility || {}) },
  };
}

function loadSettings() { //Load settings from local storage
  const raw = localStorage.getItem(SETTINGS_KEY);
  return mergeDefaults(safeParse(raw));
}

function toNumber(v) { //Convert value to number safely
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function startOfDay(d) { //Get start of day for a date
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

function formatShortDate(isoOrDate) { //Format date as "Mon DD"
  const d = new Date(isoOrDate);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}

function getIsoWeekNumber(date) { //Get ISO week number for a date
  const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())); 
  const dayNum = d.getUTCDay() || 7; 
  d.setUTCDate(d.getUTCDate() + 4 - dayNum);
  const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
  return Math.ceil(((d - yearStart) / 86400000 + 1) / 7);
}

function weekLabel(isoOrDate) { //Format date as "Wk XX"
  const d = new Date(isoOrDate);
  if (Number.isNaN(d.getTime())) return "";
  return `Wk ${getIsoWeekNumber(d)}`;
}

function monthLabel(isoOrDate) { //Format date as "Mon"
  const d = new Date(isoOrDate);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleDateString(undefined, { month: "short" }); // reutrn the month abbreviation
}

function getCssVar(name, fallback) { //Get CSS variable value with fallback
  const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  return v || fallback;
}

function buildLineOptions({ text, grid }, yTitle) { //Build chart.js line chart options
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false }, tooltip: { enabled: true } },
    scales: {
      x: { ticks: { color: text }, grid: { color: grid } },
      y: {
        ticks: { color: text },
        grid: { color: grid },
        title: yTitle
          ? { display: true, text: yTitle, color: text, font: { weight: "800" } }
          : { display: false },
      },
    },
  };
}

function convertKgToLbs(kg) {
  return kg * 2.2046226218;
}

function convertLbsToKg(lbs) {
  return lbs / 2.2046226218;
}

function convertWeight(value, fromUnit, toUnit) { //Convert weight between kg and lbs
  const from = (fromUnit || "kg").toString().toLowerCase();
  const to = (toUnit || "kg").toString().toLowerCase();
  if (from === to) return value;
  if (from === "kg" && to === "lbs") return convertKgToLbs(value);
  if (from === "lbs" && to === "kg") return convertLbsToKg(value);
  return value;
}

function normalizeWeightEntries(raw, displayUnit) { //Normalize weight entries data
  const arr = Array.isArray(raw) ? raw : [];
  return arr
    .map((e) => {
      const at = e.recordedAt;
      if (!at) return null;

      const value = toNumber(e.weightValue); 
      const unit = (e.weightUnit ?? "kg").toString().toLowerCase(); 
      const converted = convertWeight(value, unit, displayUnit); // convert to display unit

      return { id: e.id, at, value: converted }; // return normalized entry
    })
    .filter(Boolean)
    .sort((a, b) => new Date(a.at) - new Date(b.at)); // sort by date ascending
}

function normalizeMacroTrend(res) { //Normalize macro trend data
  const pts = Array.isArray(res?.points) ? res.points : [];
  return pts.map((p) => ({
    date: p.date,
    proteins: toNumber(p.proteins),
    carbs: toNumber(p.carbs),
    fats: toNumber(p.fats),
    entriesCount: toNumber(p.entriesCount), // number of food entries for the day
  }));
}

function computeWorkoutStats(completedWorkouts, baseDate = new Date()) { //Compute weekly and monthly workout stats
  const list = Array.isArray(completedWorkouts) ? completedWorkouts : [];
  const now = new Date(baseDate); // reference date

  const weekStart = startOfDay(now); // start of current week (Monday)
  const day = weekStart.getDay();
  const diff = (day === 0 ? -6 : 1) - day;
  weekStart.setDate(weekStart.getDate() + diff); // adjust to Monday

  const monthStart = startOfDay(new Date(now.getFullYear(), now.getMonth(), 1)); // start of current month

  let weeklyCount = 0;
  let monthlyCount = 0;
  let monthlyMinutes = 0;

  for (const w of list) { // iterate completed workouts
    const t = w.completedAt;
    if (!t) continue;

    const dt = new Date(t); 
    if (Number.isNaN(dt.getTime())) continue; 

    if (dt >= weekStart) weeklyCount += 1; // count for current week
    if (dt >= monthStart) {
      monthlyCount += 1;
      monthlyMinutes += toNumber(w.durationMinutes); // sum duration for current month
    }
  }

  return { weeklyCount, monthlyCount, hoursThisMonth: monthlyMinutes / 60 }; // return stats
}

function LegendToggle({ label, color, checked, onToggle }) { //Legend item with toggle functionality
  return (
    <button
      type="button"
      className="trackLegendItem"
      onClick={onToggle}
      aria-pressed={checked}
      title="Toggle series" 
    >
      <span
        className={checked ? "trackLegendBox trackLegendBoxOn" : "trackLegendBox"}
        style={{ borderColor: color }}
        aria-hidden="true"
      >
        {!checked ? <span className="trackLegendX">✕</span> : null}
      </span>
      <span className="trackLegendLabel">{label}</span>
    </button>
  );
}

function selectWeightForTab(allPts, tab) { //Select weight entries for the given tab range
  const pts = Array.isArray(allPts) ? allPts : [];
  if (pts.length === 0) return []; 

  const now = startOfDay(new Date());
  const days = tab === "daily" ? 7 : tab === "weekly" ? 60 : 180;
  const from = startOfDay(new Date(now));
  from.setDate(from.getDate() - (days - 1));

  const inRange = pts.filter((p) => {
    const dt = new Date(p.at);              // entry date
    return !Number.isNaN(dt.getTime()) && dt >= from && dt <= now; // check range
  });

  if (inRange.length > 0) return inRange;
  return pts.slice(-7); // fallback to last 7 entries
}

export default function Tracking() { //Tracking page component
  const [tab, setTab] = useState("daily");
  const [settings, setSettings] = useState(() => loadSettings());

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const [allWeightEntries, setAllWeightEntries] = useState([]);
  const [macroTrend, setMacroTrend] = useState([]);
  const [completedWorkouts, setCompletedWorkouts] = useState([]);

  const [showProtein, setShowProtein] = useState(true);
  const [showCarbs, setShowCarbs] = useState(true);
  const [showFat, setShowFat] = useState(true);

  const userId = getUserId();

  useEffect(() => { //Listen for storage events to sync settings across tabs
    const onStorage = (e) => {
      if (e.key === SETTINGS_KEY) setSettings(loadSettings());
    };
    window.addEventListener("storage", onStorage); // add listener
    return () => window.removeEventListener("storage", onStorage); // cleanup
  }, []);

  useEffect(() => {
    const t = setInterval(() => {
      const latest = loadSettings(); // reload settings
      setSettings((prev) => (JSON.stringify(prev) === JSON.stringify(latest) ? prev : latest));
    }, 400);
    return () => clearInterval(t); // cleanup on unmount
  }, []);

  const theme = useMemo(() => {
    return {
      text: getCssVar("--text", "#111827"), // default dark text
      grid: getCssVar("--border", "rgba(0,0,0,0.12)"),
      primary: getCssVar("--primary", "#0b84ff"),
      panel: getCssVar("--panel", "#f5f5f5"),
    };
  }, [settings.accessibility.mode]);

  const weightUnit = (settings.units?.weight || "kg").toString().toLowerCase(); // "kg" or "lbs"

  useEffect(() => {
    let cancelled = false;

    async function loadAll() { //Load all tracking data
      
      if (!userId) {
        setLoading(false);
        setAllWeightEntries([]);
        setMacroTrend([]);
        setCompletedWorkouts([]);
        return;
      }
      
      try {
        setLoading(true);
        setErr("");

        const range = RANGE_BY_TAB[tab] ?? "week";

        const reqs = [];

        if (settings.tracking.weight) { // weight tracking enabled
          reqs.push(apiClient.get(`/api/weight-entries/user/${userId}`)); // fetch weight entries
        } else {
          reqs.push(Promise.resolve({ data: [] })); // empty if disabled
        }

        if (settings.tracking.macros) {
          reqs.push(
            apiClient.get(`/api/nutrition-summary/user/${userId}/macro-trend`, {
              params: { range, timezone: "Europe/London" },
            })
          );
        } else {
          reqs.push(Promise.resolve({ data: { points: [] } }));
        }

        if (settings.tracking.workouts) {
          reqs.push(apiClient.get(`/api/completed-workouts/user/${userId}`));
        } else {
          reqs.push(Promise.resolve({ data: [] }));
        }

        const [wRes, mRes, cRes] = await Promise.all(reqs);
        if (cancelled) return;

        setAllWeightEntries(normalizeWeightEntries(wRes.data, weightUnit)); // normalize weight entries
        setMacroTrend(normalizeMacroTrend(mRes.data));
        setCompletedWorkouts(Array.isArray(cRes.data) ? cRes.data : []); // set completed workouts
      } catch {
        if (!cancelled) {
          setErr("Failed to load tracking data.");
          setAllWeightEntries([]);
          setMacroTrend([]);
          setCompletedWorkouts([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadAll();
    return () => {
      cancelled = true;
    };
  }, [tab, userId, settings.tracking.weight, settings.tracking.macros, settings.tracking.workouts, weightUnit]); // reload on tab, userId, or tracking settings change

  const weightEntries = useMemo(() => selectWeightForTab(allWeightEntries, tab), [allWeightEntries, tab]); // select weight entries for current tab

  const weightChart = useMemo(() => {
    const pts = weightEntries;

    if (pts.length === 0) { // no data case
      return {
        data: {
          labels: ["—"],
          datasets: [
            {
              label: `Weight (${weightUnit})`,
              data: [0],
              borderColor: theme.primary,
              backgroundColor: "rgba(0,0,0,0)",
              tension: 0.35,
              pointRadius: 0,
            },
          ],
        },
        options: buildLineOptions(theme, `Weight (${weightUnit})`), // y-axis title
      };
    }

    let labels = [];
    let values = [];

    if (tab === "daily") {
      labels = pts.map((p) => formatShortDate(p.at)); // format dates
      values = pts.map((p) => p.value); // weight values
    } else if (tab === "weekly") {
      const buckets = new Map();
      for (const p of pts) {
        const dt = new Date(p.at);
        const key = `${dt.getFullYear()}-${getIsoWeekNumber(dt)}`; // year-week key
        const prev = buckets.get(key);
        buckets.set(
          key,
          prev ? { ...prev, sum: prev.sum + p.value, n: prev.n + 1, dt } : { sum: p.value, n: 1, dt } // average per week
        );
      }
      const arr = Array.from(buckets.values()).sort((a, b) => a.dt - b.dt); // sort by date 
      labels = arr.map((x) => weekLabel(x.dt));
      values = arr.map((x) => x.sum / x.n);
    } else {
      const buckets = new Map(); // month buckets
      for (const p of pts) { // iterate weight entries
        const dt = new Date(p.at); // entry date
        const key = `${dt.getFullYear()}-${dt.getMonth()}`; // year-month key
        const prev = buckets.get(key); // average per month
        buckets.set(
          key,
          prev ? { ...prev, sum: prev.sum + p.value, n: prev.n + 1, dt } : { sum: p.value, n: 1, dt }
        );
      }
      const arr = Array.from(buckets.values()).sort((a, b) => a.dt - b.dt); // sort by date
      labels = arr.map((x) => monthLabel(x.dt)); // month labels
      values = arr.map((x) => x.sum / x.n); // average per month
    }

    return {
      data: {
        labels,
        datasets: [
          {
            label: `Weight (${weightUnit})`,
            data: values,
            borderColor: theme.primary,
            backgroundColor: "rgba(0,0,0,0)",
            tension: 0.35,
            pointRadius: 3,
            pointHoverRadius: 4,
          },
        ],
      },
      options: buildLineOptions(theme, `Weight (${weightUnit})`),
    };
  }, [weightEntries, theme, tab, weightUnit]);

  const macroColors = useMemo(() => {
    return { protein: theme.primary, carbs: "#f59e0b", fat: "#22c55e" };
  }, [theme.primary]);

  const macroChart = useMemo(() => {
    const pts = macroTrend;
    const labels = pts.map((p) => formatShortDate(p.date));

    const datasets = [
      { label: "Protein (g)", data: pts.map((p) => p.proteins), color: macroColors.protein, visible: showProtein },
      { label: "Carbs (g)", data: pts.map((p) => p.carbs), color: macroColors.carbs, visible: showCarbs },
      { label: "Fat (g)", data: pts.map((p) => p.fats), color: macroColors.fat, visible: showFat },
    ];

    return {
      data: {
        labels,
        datasets: datasets.map((d) => ({
          label: d.label,
          data: d.data,
          borderColor: d.color,
          backgroundColor: d.color,
          tension: 0.35,
          pointRadius: 2,
          hidden: !d.visible,
        })),
      },
      options: buildLineOptions(theme, "Grams (g)"),
    };
  }, [macroTrend, theme, macroColors, showProtein, showCarbs, showFat]);

  const workoutStats = useMemo(() => {
    const s = computeWorkoutStats(completedWorkouts, new Date()); // compute stats based on completed workouts
    return {
      weekly: `${s.weeklyCount}/${WEEKLY_TARGET}`,
      monthly: `${s.monthlyCount}/${MONTHLY_TARGET}`,
      hoursMonth: `${Math.max(0, s.hoursThisMonth).toFixed(0)}hrs`,
    };
  }, [completedWorkouts]);

  return (
    <div className="pageShell">
      <HeaderBar title="Tracking" />

      <div className="pageBody trackBody">
        <div className="trackTabs">
          <button type="button" className={tab === "daily" ? "trackTab trackTabOn" : "trackTab"} onClick={() => setTab("daily")}>
            Daily
          </button>
          <button type="button" className={tab === "weekly" ? "trackTab trackTabOn" : "trackTab"} onClick={() => setTab("weekly")}>
            Weekly
          </button>
          <button type="button" className={tab === "monthly" ? "trackTab trackTabOn" : "trackTab"} onClick={() => setTab("monthly")}>
            Monthly
          </button>
        </div>

        {err && <div className="trackHint">{err}</div>}
        {loading && <div className="trackHint">Loading…</div>}

        {!settings.tracking.weight ? (
          <div className="trackCard">
            <div className="trackCardTitle">Weight Progress Over Time</div>
            <div className="trackDisabled">Weight tracking is disabled in Settings.</div>
          </div>
        ) : (
          <div className="trackCard">
            <div className="trackCardTitle">Weight Progress Over Time</div>
            <div className="trackChart trackChartReal" style={{ background: theme.panel }}>
              <Line data={weightChart.data} options={weightChart.options} />
            </div>
          </div>
        )}

        {!settings.tracking.macros ? (
          <div className="trackCard">
            <div className="trackCardTitle">Daily Macro Intake Trends</div>
            <div className="trackDisabled">Macro tracking is disabled in Settings.</div>
          </div>
        ) : (
          <div className="trackCard">
            <div className="trackCardTitle">Daily Macro Intake Trends</div>

            <div className="trackLegend">
              <LegendToggle label="Protein" color={macroColors.protein} checked={showProtein} onToggle={() => setShowProtein((v) => !v)} />
              <LegendToggle label="Carbs" color={macroColors.carbs} checked={showCarbs} onToggle={() => setShowCarbs((v) => !v)} />
              <LegendToggle label="Fat" color={macroColors.fat} checked={showFat} onToggle={() => setShowFat((v) => !v)} />
            </div>

            <div className="trackChart trackChartReal" style={{ background: theme.panel }}>
              <Line data={macroChart.data} options={macroChart.options} />
            </div>
          </div>
        )}

        {!settings.tracking.workouts ? (
          <div className="trackCard">
            <div className="trackCardTitle">Workouts Completed</div>
            <div className="trackDisabled">Workout tracking is disabled in Settings.</div>
          </div>
        ) : (
          <div className="trackCard">
            <div className="trackCardTitle">Workouts Completed</div>

            <div className="trackStats">
              <div className="trackStatRow">
                <span className="trackStatLabel">Weekly Completed:</span>
                <span className="trackStatValue">{workoutStats.weekly}</span>
              </div>
              <div className="trackStatRow">
                <span className="trackStatLabel">Monthly Completed:</span>
                <span className="trackStatValue">{workoutStats.monthly}</span>
              </div>
              <div className="trackStatRow">
                <span className="trackStatLabel">Hours Spent This Month:</span>
                <span className="trackStatValue">{workoutStats.hoursMonth}</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
