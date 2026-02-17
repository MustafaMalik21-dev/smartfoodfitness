import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import "./WorkoutHistory.css";
import { getUserId } from "../auth/authStorage";

function formatWhen(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString(undefined, {
    weekday: "short",
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function safeJsonParse(s) {
  try {
    return JSON.parse(s);
  } catch {
    return null;
  }
}

function getDoneSetCount(ex) {
  const sets = Array.isArray(ex?.sets) ? ex.sets : [];
  return sets.reduce((acc, st) => acc + (st?.done ? 1 : 0), 0);
}

function getTotalSetCount(ex) {
  const sets = Array.isArray(ex?.sets) ? ex.sets : [];
  return sets.length;
}

function displayWeight(w) {
  if (w === null || w === undefined) return "";
  const s = String(w).trim();
  if (!s) return "";
  return s;
}

function displayReps(r) {
  if (r === null || r === undefined) return "";
  const s = String(r).trim();
  if (!s) return "";
  return s;
}

function exerciseSummary(ex) {
  const name = ex?.name || "Exercise";
  const done = getDoneSetCount(ex);
  const total = getTotalSetCount(ex);
  if (!total) return `${name}`;
  return `${name} (${done}/${total})`;
}

function makeExerciseKey(logId, exIndex) {
  return `${logId}::${exIndex}`;
}

export default function WorkoutHistory() {
  const navigate = useNavigate();
  const userId = getUserId();

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const [openExerciseKey, setOpenExerciseKey] = useState("");

  async function load() {
    if (!userId) return;

    setLoading(true);
    setErr("");

    try {
      const res = await apiClient.get(`/api/workout-logs/user/${userId}`);
      const data = Array.isArray(res.data) ? res.data : [];
      setLogs(data);
    } catch {
      setLogs([]);
      setErr("Could not load workout history.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [userId]);

  const view = useMemo(() => {
    return (logs || [])
      .slice()
      .sort((a, b) => {
        const ta = new Date(a?.performedAt || 0).getTime();
        const tb = new Date(b?.performedAt || 0).getTime();
        return tb - ta;
      })
      .map((l) => {
        const details = typeof l.detailsJson === "string" ? safeJsonParse(l.detailsJson) : null;
        const exercises = Array.isArray(details?.exercises) ? details.exercises : [];

        const totalExercises = exercises.length;
        const completedExercises = exercises.reduce((acc, ex) => acc + (getDoneSetCount(ex) > 0 ? 1 : 0), 0);

        const topExercises = exercises.slice(0, 5).map(exerciseSummary);

        return {
          id: l.id,
          performedAt: l.performedAt,
          workoutName: l.workoutName,
          workoutType: l.workoutType,
          durationMinutes: l.durationMinutes,
          notes: l.notes,
          totalExercises,
          completedExercises,
          topExercises,
          exercises,
        };
      });
  }, [logs]);

  function toggleExercise(logId, exIndex) {
    const key = makeExerciseKey(logId, exIndex);
    setOpenExerciseKey((prev) => (prev === key ? "" : key));
  }

  return (
    <div className="pageShell">
      <div className="whTopBar">
        <div className="whTitle">Workout History</div>

        <div className="whTopActions">
          <button className="whTopBtn" type="button" onClick={() => navigate(-1)}>
            Back
          </button>
          <button className="whTopBtn" type="button" onClick={load} disabled={loading}>
            Refresh
          </button>
        </div>
      </div>

      <div className="pageBody whBody">
        {loading ? <div className="whInfo">Loading…</div> : null}
        {err ? <div className="whError">{err}</div> : null}

        {!loading && !err && view.length === 0 ? (
          <div className="whEmpty">
            <div className="whEmptyTitle">No workouts yet</div>
            <div className="whEmptySub">Once you complete workouts, they’ll show here.</div>
          </div>
        ) : null}

        <div className="whList">
          {view.map((item) => (
            <div className="whCard" key={item.id}>
              <div className="whCardHeader">
                <div className="whCardLeft">
                  <div className="whWorkoutName">{item.workoutName}</div>
                  <div className="whWhen">{formatWhen(item.performedAt)}</div>
                </div>

                <div className="whCardRight">
                  {item.durationMinutes != null ? <div className="whPill">{item.durationMinutes} min</div> : null}
                  <div className="whPill whPillSoft">
                    {item.completedExercises}/{item.totalExercises} done
                  </div>
                </div>
              </div>

              {item.notes ? <div className="whNotes">{item.notes}</div> : null}

              {item.exercises.length ? (
                <div className="whExercises">
                  {item.exercises.map((ex, idx) => {
                    const done = getDoneSetCount(ex);
                    const total = getTotalSetCount(ex);
                    const isOpen = openExerciseKey === makeExerciseKey(item.id, idx);
                    const sets = Array.isArray(ex?.sets) ? ex.sets : [];

                    return (
                      <div className="whExerciseBlock" key={`${item.id}-${idx}`}>
                        <button
                          type="button"
                          className={`whExerciseRow ${isOpen ? "whExerciseRowOpen" : ""}`}
                          onClick={() => toggleExercise(item.id, idx)}
                        >
                          <div className="whExName">{ex?.name || "Exercise"}</div>
                          <div className="whExRight">
                            <div className={`whExPill ${done > 0 ? "whExPillDone" : ""}`}>
                              {total ? `${done}/${total}` : "—"}
                            </div>
                            <div className={`whChevron ${isOpen ? "whChevronOpen" : ""}`}>⌄</div>
                          </div>
                        </button>

                        {isOpen ? (
                          <div className="whSetsPanel">
                            {sets.length ? (
                              <div className="whSetsList">
                                {sets.map((st, sIdx) => {
                                  const w = displayWeight(st?.weight);
                                  const r = displayReps(st?.reps);
                                  const doneSet = !!st?.done;

                                  return (
                                    <div className={`whSetRow ${doneSet ? "whSetRowDone" : ""}`} key={`${item.id}-${idx}-${sIdx}`}>
                                      <div className="whSetLeft">
                                        <div className="whSetNum">Set {sIdx + 1}</div>
                                        <div className="whSetMeta">
                                          {w ? <span className="whSetTag">{w}</span> : <span className="whSetTag whSetTagSoft">—</span>}
                                          {r ? <span className="whSetTag">{r} reps</span> : <span className="whSetTag whSetTagSoft">— reps</span>}
                                        </div>
                                      </div>
                                      <div className={`whSetStatus ${doneSet ? "whSetStatusDone" : ""}`}>{doneSet ? "Done" : "Not done"}</div>
                                    </div>
                                  );
                                })}
                              </div>
                            ) : (
                              <div className="whNoSets">No set details recorded.</div>
                            )}
                          </div>
                        ) : null}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="whNoExercises">No exercise details recorded.</div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
