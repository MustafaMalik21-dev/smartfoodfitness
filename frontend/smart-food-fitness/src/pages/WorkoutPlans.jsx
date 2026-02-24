import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "./WorkoutPlans.css";

function levelBtnClass(active) { // Helper function to determine the CSS class for the level filter buttons based on whether they are active or not, returning a string that includes the base class "wpSegBtn" and conditionally adds "wpSegBtnOn" if the button is active, allowing for dynamic styling of the filter buttons in the workout plans page to indicate which level is currently selected by the user.
  return active ? "wpSegBtn wpSegBtnOn" : "wpSegBtn";
}

function goalBtnClass(active) {
  return active ? "wpSegBtn wpSegBtnOn" : "wpSegBtn";
}

export default function WorkoutPlans() { // Main component for the workout plans page, responsible for fetching and displaying a list of workout plans based on the selected level and goal filters. The component manages the state of the workout plans, loading status, and any errors that may occur during data fetching. It also provides functionality to toggle the visibility of additional details for each workout plan and to navigate to the full plan view when a plan is selected. The component uses helper functions to determine the CSS classes for the filter buttons and to generate unique keys for toggling plan details, ensuring an interactive and user-friendly interface for browsing workout plans.
  const navigate = useNavigate();

  const [level, setLevel] = useState("Beginner");
  const [goal, setGoal] = useState("Fat Loss");

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const [plans, setPlans] = useState([]);

  const [openId, setOpenId] = useState(null);

  const params = useMemo(() => {
    const p = {};
    if (level) p.level = level;
    if (goal) p.goal = goal;
    return p;
  }, [level, goal]);

  useEffect(() => { // Effect hook to load the workout plans based on the selected level and goal filters, making an API request to fetch the plans and updating the component state accordingly. The effect handles loading state, error handling, and cancellation to prevent state updates on unmounted components, ensuring a smooth user experience when filtering workout plans or when navigating away from the page before the data fetching completes.
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setErr("");
        const res = await apiClient.get("/api/workout-plans/search", { params });
        if (!cancelled) setPlans(Array.isArray(res.data) ? res.data : []);
      } catch {
        if (!cancelled) setErr("Could not load workout plans.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [params]);

  return (
    <div className="pageShell">
      <div className="wpTop">
        <button className="wpBackBtn" type="button" onClick={() => navigate("/fitness")}>
          Back
        </button>

        <div className="wpTitle">Workout Plans</div>

        <div className="wpTopSpacer" />
      </div>

      <div className="pageBody wpBody">
        <div className="wpHint">Choose a plan to fit your experience and goals</div>
        <div className="wpSegWrap" role="tablist" aria-label="Level filter">
          {["Beginner", "Intermediate", "Advanced"].map((x) => (
            <button
              key={x}
              type="button"
              role="tab"
              aria-selected={level === x}
              className={levelBtnClass(level === x)}
              onClick={() => {
                setOpenId(null);
                setLevel(x);
              }}
            >
              {x}
            </button>
          ))}
        </div>

        <div className="wpSegWrap wpSegWrap4" role="tablist" aria-label="Goal filter">
          {["Strength", "Muscle Gain", "Fat Loss", "General Fitness"].map((x) => (
            <button
              key={x}
              type="button"
              role="tab"
              aria-selected={goal === x}
              className={goalBtnClass(goal === x)}
              onClick={() => {
                setOpenId(null);
                setGoal(x);
              }}
            >
              {x}
            </button>
          ))}
        </div>

        {loading ? <div className="wpMsg">Loading plans…</div> : null}
        {err ? <div className="wpErr">{err}</div> : null}

        {!loading && !err && plans.length === 0 ? <div className="wpMsg">No plans found for these filters.</div> : null}

        {plans.map((p) => {
          const isOpen = openId === p.id;

          return (
            <div key={p.id} className="wpCard">
              <div className="wpCardTop">
                <div className="wpPlanTitle">{p.title}</div>
                <div className="wpPlanSub">{p.shortDescription}</div>
              </div>

              <div className="wpProsCons">
                <div className="wpPro">🟢 {p.pros}</div>
                <div className="wpCon">🔴 {p.cons}</div>

                <button type="button" className="wpMore" onClick={() => setOpenId(isOpen ? null : p.id)}>
                  {isOpen ? "[ ▴ Less Details ]" : "[ ▾ More Details ]"}
                </button>
              </div>

              <div className="wpMetaCol">
                <span className="wpMeta">{p.level}</span>
                <span className="wpMeta">{p.goal}</span>
                <span className="wpMeta">Split | {p.split}</span>
                <span className="wpMeta">Days/Week | {p.daysPerWeek ?? "—"}</span>
                <span className="wpMeta">Time ~ {p.estimatedDurationMinutes ?? "—"}m</span>
              </div>

              {isOpen ? (
                <div className="wpDetails">
                  <div className="wpDetailsLine">
                    <b>Split:</b> {p.split}
                  </div>
                  <div className="wpDetailsLine">
                    <b>Goal:</b> {p.goal}
                  </div>
                  <div className="wpDetailsLine">
                    <b>Level:</b> {p.level}
                  </div>
                </div>
              ) : null}

              <button type="button" className="wpViewPlan" onClick={() => navigate(`/workout-plans/${p.id}`)}>
                View full plan
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
