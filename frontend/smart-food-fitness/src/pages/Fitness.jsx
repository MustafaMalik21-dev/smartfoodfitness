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

function ymd(d) { // Helper function that takes a Date object and returns a string in the format "YYYY-MM-DD", used for creating keys for the schedule map and displaying selected dates
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function buildWorkoutWeekdays(daysPerWeek) { // Helper function that takes the number of workout days per week and returns an array of weekday indices (0 for Sunday, 1 for Monday, etc.) representing the workout days, based on a predefined pattern for different numbers of workout days
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

function firstDateInGrid(year, month) { // Helper function that calculates the first date to be displayed in the calendar grid for a given month and year, which is the Sunday on or before the first day of the month, ensuring that the grid always starts on a Sunday and includes all days of the month
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

function buildScheduleMap({ year, month, weekdays, sessions }) { // Helper function that builds a schedule map for the given month and year based on the workout weekdays and sessions, creating a mapping of date strings (in "YYYY-MM-DD" format) to session details for each workout day in the month, allowing for easy lookup of scheduled workouts when rendering the calendar and selected session details
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

export default function Fitness() { // Main component for the Fitness page, responsible for displaying the user's workout schedule in a calendar format, showing a preview of the next workout session, and providing navigation to other fitness-related pages, while managing state for the calendar, workout sessions, and user profile data
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

  useEffect(() => { // Effect hook that runs on component mount and whenever the userId changes, responsible for fetching the user's profile data to determine the selected workout plan, fetching the workout plan details and sessions, and updating the state with the fetched data to display the workout schedule and next session preview, while handling cancellation to prevent state updates on unmounted components
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

        const planRes = await apiClient.get(`/api/workout-plans/${planId}`); // Fetch the details of the selected workout plan, including the number of workout days per week, which is used to determine the workout schedule and session mapping for the calendar display
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

  useEffect(() => { // Effect hook that runs whenever the workout plan days per week, sessions, or calendar month/year changes, responsible for rebuilding the schedule map based on the current workout plan and calendar view, and updating the selected date if it falls outside the current month, ensuring that the calendar display and selected session details are always in sync with the user's workout plan and the currently displayed month
    const weekdays = buildWorkoutWeekdays(planDaysPerWeek);
    const map = buildScheduleMap({ year: calYear, month: calMonth, weekdays, sessions });
    setScheduleMap(map);

    const selectedDate = new Date(calYear, calMonth, Number(selectedKey.slice(8, 10)));
    if (selectedDate.getMonth() !== calMonth || selectedDate.getFullYear() !== calYear) {
      setSelectedKey(ymd(new Date(calYear, calMonth, 1)));
    }
  }, [planDaysPerWeek, sessions, calYear, calMonth]);

  const gridDays = useMemo(() => buildMonthGrid(calYear, calMonth), [calYear, calMonth]);

  function goMonth(delta) { // Function that handles changing the calendar month when the user clicks the previous or next month buttons, calculating the new month and year based on the current month/year and the delta, and updating the state to trigger a re-render of the calendar with the new month
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

        <button type="button" className="bigPill" onClick={() => navigate("/workout-history")}>
          Workout History
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
