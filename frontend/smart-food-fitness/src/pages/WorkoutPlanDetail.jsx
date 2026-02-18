import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import "./WorkoutPlanDetail.css";

function safeParse(json) {
  try {
    const v = JSON.parse(json);
    return Array.isArray(v) ? v : [];
  } catch {
    return [];
  }
}

export default function WorkoutPlanDetail() {
  const navigate = useNavigate();
  const { id } = useParams();

  const userId = useMemo(() => getUserId(), []);

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const [plan, setPlan] = useState(null);
  const [sessions, setSessions] = useState([]);

  const [selectBusy, setSelectBusy] = useState(false);
  const [selectMsg, setSelectMsg] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setErr("");
        setSelectMsg("");

        const [planRes, sesRes] = await Promise.all([
          apiClient.get(`/api/workout-plans/${id}`),
          apiClient.get(`/api/workout-plans/${id}/sessions`),
        ]);

        if (cancelled) return;

        setPlan(planRes.data || null);
        setSessions(Array.isArray(sesRes.data) ? sesRes.data : []);
      } catch {
        if (!cancelled) setErr("Could not load this workout plan.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function selectPlan() {
    if (!userId) {
      setSelectMsg("No userId found. Log in again.");
      return;
    }

    try {
      setSelectBusy(true);
      setSelectMsg("");
      await apiClient.post(`/api/workout-plans/${id}/select`, null, { params: { userId } });
      setSelectMsg("Plan selected ✅");
    } catch {
      setSelectMsg("Could not select plan.");
    } finally {
      setSelectBusy(false);
    }
  }

  return (
    <div className="pageShell">
      <div className="wpdTop">
        <button className="wpdBackBtn" type="button" onClick={() => navigate("/workout-plans")}>
          Back
        </button>
        <div className="wpdTitle">Plan Details</div>
        <div className="wpdTopSpacer" />
      </div>

      <div className="pageBody wpdBody">
        {loading ? <div className="wpdMsg">Loading…</div> : null}
        {err ? <div className="wpdErr">{err}</div> : null}

        {!loading && !err && plan ? (
          <>
            <div className="wpdCard">
              <div className="wpdPlanTitle">{plan.title}</div>
              <div className="wpdPlanSub">{plan.shortDescription}</div>

              <div className="wpdMetaGrid">
                <span className="wpdMeta">{plan.level}</span>
                <span className="wpdMeta">{plan.goal}</span>
                <span className="wpdMeta">Split | {plan.split}</span>
                <span className="wpdMeta">Days/Week | {plan.daysPerWeek ?? "—"}</span>
                <span className="wpdMeta">Time ~ {plan.estimatedDurationMinutes ?? "—"}m</span>
              </div>

              <div className="wpdProsCons">
                <div className="wpdPro">🟢 {plan.pros}</div>
                <div className="wpdCon">🔴 {plan.cons}</div>
              </div>

              <button className="wpdPrimary" type="button" onClick={selectPlan} disabled={selectBusy}>
                {selectBusy ? "Selecting…" : "Select this plan"}
              </button>
              {selectMsg ? <div className="wpdMsg2">{selectMsg}</div> : null}
            </div>

            <div className="wpdCard">
              <div className="wpdSectionTitle">Sessions</div>

              {sessions.length === 0 ? <div className="wpdMsg">No sessions found.</div> : null}

              {sessions.map((s) => {
                const ex = safeParse(s.exerciseJson);
                return (
                  <div key={s.id} className="wpdSession">
                    <div className="wpdSessionTop">
                      <div className="wpdSessionName">{s.title}</div>
                      <div className="wpdSessionMeta">
                        {s.focus} • {s.estimatedMinutes ?? "—"}m
                      </div>
                    </div>

                    <div className="wpdList">
                      {ex.slice(0, 8).map((e, idx2) => (
                        <div key={idx2} className="wpdRow">
                          <div className="wpdRowName">{e.name}</div>
                          <div className="wpdRowMeta">
                            {e.sets ? `${e.sets} sets` : "—"} • {e.reps ? `${e.reps} reps` : "—"}
                          </div>
                        </div>
                      ))}
                    </div>

                    <button
                      type="button"
                      className="wpdSecondary"
                      onClick={() => navigate("/workout")}
                    >
                      Start workout
                    </button>
                  </div>
                );
              })}
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
