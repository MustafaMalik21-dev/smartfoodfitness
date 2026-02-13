import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeaderBar from "../components/HeaderBar";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import "../styles/PageShell.css";
import "./Fitness.css";

import DumbbellIcon from "../assets/Dumbellicon.png";

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
const WEEKDAYS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];

function ymd(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function buildWorkoutWeekdays(daysPerWeek) {
  const d = Number(daysPerWeek || 0);
  if (d <= 0) return [];
  if (d === 1) return [1];
  if (d === 2) return [2, 5];
  if (d === 3) return [1, 3, 5];
  if (d === 4) return [1, 2, 4, 5];
  if (d === 5) return [1, 2, 3, 5, 6];
  if (d === 6) return [1, 2, 3, 4, 5, 6];
  return [0, 1, 2, 3, 4, 5, 6];
}

function firstDateInGrid(year, month) {
  const first = new Date(year, month, 1);
  const dow = first.getDay();
  const start = new Date(year, month, 1 - dow);
  start.setHours(12, 0, 0, 0);
  return start;
}

function buildMonthGrid(year, month) {
  const start = firstDateInGrid(year, month);
  const days = [];
  for (let i = 0; i < 42; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    days.push(d);
  }
  return days;
}

function buildScheduleMap({ year, month, weekdays, sessions }) {
  if (!weekdays || weekdays.length === 0 || !sessions || sessions.length === 0) return {};

  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const monthDates = [];
  for (let day = 1; day <= daysInMonth; day++) {
    const d = new Date(year, month, day);
    d.setHours(12, 0, 0, 0);
    if (weekdays.includes(d.getDay())) monthDates.push(d);
  }

  const map = {};
  let sIdx = 0;
  for (const d of monthDates) {
    const key = ymd(d);
    const sess = sessions[sIdx % sessions.length];
    map[key] = {
      title: sess?.title || "Workout",
      sessionIndex: sess?.sessionIndex ?? null,
      exercises: sess?.exercises || [],
    };
    sIdx++;
  }
  return map;
}

export default function Fitness() {
  const navigate = useNavigate();
  const userId = useMemo(() => getUserId(), []);

  const now = useMemo(() => new Date(), []);
  const [calMonth, setCalMonth] = useState(now.getMonth());
  const [calYear, setCalYear] = useState(now.getFullYear());
  const [selectedKey, setSelectedKey] = useState(ymd(now));

  const [preview, setPreview] = useState(null);

  const [planDaysPerWeek, setPlanDaysPerWeek] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [scheduleMap, setScheduleMap] = useState({});
  const [schedMsg, setSchedMsg] = useState("");

  useEffect(() => {
    if (!userId) return;

    let cancelled = false;

    async function loadFitnessData() {
      try {
        setSchedMsg("");
        const prof = await apiClient.get(`/api/user-profile/${userId}`);
        const planId = prof?.data?.selectedWorkoutPlanId;

        if (!planId) {
          if (!cancelled) {
            setPreview(null);
            setPlanDaysPerWeek(null);
            setSessions([]);
            setScheduleMap({});
            setSchedMsg("Select a plan to see your schedule.");
          }
          return;
        }

        const planRes = await apiClient.get(`/api/workout-plans/${planId}`);
        const daysPerWeek = planRes?.data?.daysPerWeek ?? null;

        const sessRes = await apiClient.get(`/api/workout-plan-sessions/plan/${planId}`);
        const sess = Array.isArray(sessRes.data) ? sessRes.data : [];

        const first = sess[0] || null;

        if (!cancelled) {
          setPlanDaysPerWeek(daysPerWeek);
          setSessions(sess);

          setPreview(
            first
              ? {
                  planId,
                  title: first.title,
                  exerciseNames: (first.exercises || []).slice(0, 3).map((e) => e.name).join(", "),
                }
              : null
          );
        }
      } catch {
        if (!cancelled) {
          setPreview(null);
          setPlanDaysPerWeek(null);
          setSessions([]);
          setScheduleMap({});
          setSchedMsg("Could not load schedule.");
        }
      }
    }

    loadFitnessData();
    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    const weekdays = buildWorkoutWeekdays(planDaysPerWeek);
    const map = buildScheduleMap({ year: calYear, month: calMonth, weekdays, sessions });
    setScheduleMap(map);

    const selectedDate = new Date(calYear, calMonth, Number(selectedKey.slice(8, 10)));
    if (selectedDate.getMonth() !== calMonth || selectedDate.getFullYear() !== calYear) {
      setSelectedKey(ymd(new Date(calYear, calMonth, 1)));
    }
  }, [planDaysPerWeek, sessions, calYear, calMonth]);

  const gridDays = useMemo(() => buildMonthGrid(calYear, calMonth), [calYear, calMonth]);

  function goMonth(delta) {
    const d = new Date(calYear, calMonth + delta, 1);
    setCalYear(d.getFullYear());
    setCalMonth(d.getMonth());
  }

  const years = useMemo(() => {
    const y = new Date().getFullYear();
    const arr = [];
    for (let i = y - 2; i <= y + 2; i++) arr.push(i);
    return arr;
  }, []);

  const selectedSession = scheduleMap[selectedKey] || null;

  return (
    <div className="pageShell">
      <HeaderBar title="Fitness" left="profile" right="notifications" />

      <div className="pageBody fitnessBody">
        <button type="button" className="bigPill" onClick={() => navigate("/exercise-encyclopedia")}>
          Exercise Encyclopedia
        </button>

        <button type="button" className="bigPill" onClick={() => navigate("/workout")}>
          Workout
        </button>

        <button type="button" className="bigPill" onClick={() => navigate("/workout-plans")}>
          Workout Plans
        </button>

        <div className="card">
          <div className="cardTitle">Workout Schedule</div>

          <div className="calWrap">
            <button type="button" className="calArrow" onClick={() => goMonth(-1)} aria-label="Previous month">
              ‹
            </button>

            <div className="calSelectors">
              <select
                className="calSelect"
                value={calMonth}
                onChange={(e) => setCalMonth(Number(e.target.value))}
                aria-label="Month"
              >
                {MONTHS.map((m, i) => (
                  <option key={m} value={i}>
                    {m}
                  </option>
                ))}
              </select>

              <select
                className="calSelect"
                value={calYear}
                onChange={(e) => setCalYear(Number(e.target.value))}
                aria-label="Year"
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>

            <button type="button" className="calArrow" onClick={() => goMonth(1)} aria-label="Next month">
              ›
            </button>
          </div>

          <div className="calGrid">
            {WEEKDAYS.map((w) => (
              <div className="calDow" key={w}>
                {w}
              </div>
            ))}

            {gridDays.map((d) => {
              const inMonth = d.getMonth() === calMonth;
              const key = ymd(d);
              const hasWorkout = !!scheduleMap[key];
              const isSelected = key === selectedKey;

              return (
                <button
                  key={key}
                  type="button"
                  className={"calCell" + (inMonth ? "" : " calCellOff") + (isSelected ? " calCellOn" : "")}
                  onClick={() => setSelectedKey(key)}
                  aria-pressed={isSelected}
                >
                  <span className="calDay">{d.getDate()}</span>

                  {hasWorkout ? (
                    <img className="calDotIcon" src={DumbbellIcon} alt="" aria-hidden="true" />
                  ) : (
                    <span className="calDotSpacer" aria-hidden="true" />
                  )}
                </button>
              );
            })}
          </div>

          {schedMsg ? <div className="cardSub" style={{ marginTop: 10 }}>{schedMsg}</div> : null}

          {!schedMsg ? (
            <div className="calSelected">
              <div className="calSelectedTop">
                <span className="calSelectedDate">{selectedKey}</span>
                {selectedSession ? (
                  <span className="calSelectedBadge">Workout</span>
                ) : (
                  <span className="calSelectedBadge calSelectedBadgeOff">Rest</span>
                )}
              </div>

              <div className={selectedSession ? "calSelectedTitle calSelectedTitleOn" : "calSelectedTitle"}>
                {selectedSession ? selectedSession.title : "No session planned for this day."}
              </div>
            </div>
          ) : null}
        </div>

        <div className="card">
          <div className="cardTitle">Next Session Preview</div>
          {preview ? (
            <div className="cardSub">
              <b>{preview.title}</b>
              <br />
              Exercises: {preview.exerciseNames || "—"}
            </div>
          ) : (
            <div className="cardSub">Select a plan to see your next session.</div>
          )}

          <button type="button" className="smallPill" onClick={() => navigate("/workout")}>
            Start Workout
          </button>
        </div>
      </div>
    </div>
  );
}
