// src/pages/WorkoutPlans.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "./WorkoutPlans.css";

function levelBtnClass(active) {
  return active ? "wpSegBtn wpSegBtnOn" : "wpSegBtn";
}

function goalBtnClass(active) {
  return active ? "wpSegBtn wpSegBtnOn" : "wpSegBtn";
}

export default function WorkoutPlans() {
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

  useEffect(() => {
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
    <div className="wpPage">
      <div className="wpTop">
        <button className="wpBackBtn" type="button" onClick={() => navigate("/fitness")}>
          Back
        </button>

        <div className="wpTitle">Workout Plans</div>

        <div className="wpTopSpacer" />
      </div>

      <div className="wpBody">
        <div className="wpHint">Choose a plan to fit your experience and goals</div>

        {/* Segmented control: Level (top 3) */}
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

        {/* Segmented control: Goal (bottom 4) */}
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
