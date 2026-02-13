import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import "../styles/PageShell.css";
import "./Workout.css";

function cleanHtmlToText(html) {
  if (!html) return "";
  return String(html).replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();
}

function fmtTime(totalSec) {
  const s = Math.max(0, totalSec | 0);
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const ss = s % 60;
  return `${h}h ${String(m).padStart(2, "0")}m ${String(ss).padStart(2, "0")}s`;
}

function buildDefaultSets(targetSets = 3, reps = 8) {
  const n = Math.max(1, Math.min(Number(targetSets) || 3, 12));
  const r = String(Number(reps) || 8);
  return Array.from({ length: n }).map(() => ({
    weight: "",
    reps: r,
    done: false,
  }));
}

function pickBestSearchHit(list, wantedName) {
  const wanted = String(wantedName || "").trim().toLowerCase();
  if (!wanted || !Array.isArray(list) || list.length === 0) return null;

  const exact = list.find((x) => String(x?.name || "").trim().toLowerCase() === wanted);
  if (exact) return exact;

  const contains = list.find((x) => String(x?.name || "").trim().toLowerCase().includes(wanted));
  if (contains) return contains;

  return list[0];
}

export default function Workout() {
  const navigate = useNavigate();
  const userId = useMemo(() => getUserId(), []);

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const [planId, setPlanId] = useState(null);
  const [sessionTitle, setSessionTitle] = useState("Workout");
  const [exercises, setExercises] = useState([]);
  const [idx, setIdx] = useState(0);

  const [elapsedSec, setElapsedSec] = useState(0);
  const timerRef = useRef(null);

  const [metaByName, setMetaByName] = useState({});
  const metaByNameRef = useRef({});
  useEffect(() => {
    metaByNameRef.current = metaByName;
  }, [metaByName]);

  const current = exercises[idx] || null;

  useEffect(() => {
    setElapsedSec(0);
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => setElapsedSec((x) => x + 1), 1000);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      setErr("No user is logged in.");
      return;
    }

    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setErr("");

        const prof = await apiClient.get(`/api/user-profile/${userId}`);
        const selected = prof?.data?.selectedWorkoutPlanId;

        setPlanId(selected || null);

        if (!selected) {
          if (!cancelled) {
            setSessionTitle("No plan selected");
            setExercises([]);
            setIdx(0);
          }
          return;
        }

        const s = await apiClient.get(`/api/workout-plan-sessions/plan/${selected}`);
        const sessions = Array.isArray(s.data) ? s.data : [];
        const first = sessions[0];

        if (!first) {
          if (!cancelled) {
            setSessionTitle("No sessions found");
            setExercises([]);
            setIdx(0);
          }
          return;
        }

        const raw = Array.isArray(first.exercises) ? first.exercises : [];

        const normalized = raw
          .map((x, i) => {
            const name = String(x?.name || "").trim();
            if (!name) return null;

            const setsCount = Number(x?.sets ?? 3);
            const reps = Number(x?.reps ?? 8);

            return {
              key: `${name}-${i}`,
              name,
              notes: String(x?.notes || ""),
              sets: buildDefaultSets(setsCount, reps),
            };
          })
          .filter(Boolean);

        if (!cancelled) {
          setSessionTitle(first.title || "Workout");
          setExercises(normalized);
          setIdx(0);
        }
      } catch {
        if (!cancelled) {
          setSessionTitle("Workout");
          setExercises([]);
          setIdx(0);
          setErr("Could not load workout session.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    if (!exercises || exercises.length === 0) return;

    let cancelled = false;

    async function loadMetaForName(name) {
      const n = String(name || "").trim();
      if (!n) return;

      const existing = metaByNameRef.current[n];
      if (existing && existing.status !== "error") return;

      setMetaByName((m) => ({
        ...m,
        [n]: { status: "loading", description: "", image: "" },
      }));

      try {
        const searchRes = await apiClient.get("/api/exercises/search", {
          params: { q: n, limit: 25, scope: "all" },
        });

        if (cancelled) return;

        const list = Array.isArray(searchRes.data) ? searchRes.data : [];
        const hit = pickBestSearchHit(list, n);

        if (!hit?.id) {
          setMetaByName((m) => ({
            ...m,
            [n]: { status: "done", description: "", image: "" },
          }));
          return;
        }

        const detailRes = await apiClient.get(`/api/exercises/${hit.id}`);
        if (cancelled) return;

        const imgs = Array.isArray(detailRes.data?.images) ? detailRes.data.images : [];
        const image = (imgs.find(Boolean) || hit.imageUrl || "").trim();
        const description = cleanHtmlToText(detailRes.data?.description || "");

        setMetaByName((m) => ({
          ...m,
          [n]: { status: "done", description, image },
        }));
      } catch {
        setMetaByName((m) => ({
          ...m,
          [n]: { status: "error", description: "", image: "" },
        }));
      }
    }

    const uniqueNames = Array.from(new Set(exercises.map((x) => x.name).filter(Boolean)));

    (async () => {
      for (const name of uniqueNames) {
        if (cancelled) break;
        await loadMetaForName(name);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [exercises]);

  function setIdxSafe(next) {
    const clamped = Math.max(0, Math.min(exercises.length - 1, next));
    setIdx(clamped);
  }

  function toggleDone(setIndex) {
    setExercises((prev) =>
      prev.map((ex, exI) => {
        if (exI !== idx) return ex;
        return {
          ...ex,
          sets: ex.sets.map((s, i) => (i === setIndex ? { ...s, done: !s.done } : s)),
        };
      })
    );
  }

  function editSet(setIndex, field, value) {
    setExercises((prev) =>
      prev.map((ex, exI) => {
        if (exI !== idx) return ex;
        return {
          ...ex,
          sets: ex.sets.map((s, i) => (i === setIndex ? { ...s, [field]: value } : s)),
        };
      })
    );
  }

  function addSet() {
    setExercises((prev) =>
      prev.map((ex, exI) => {
        if (exI !== idx) return ex;
        const last = ex.sets[ex.sets.length - 1] || { weight: "", reps: "8", done: false };
        return {
          ...ex,
          sets: ex.sets.concat([{ weight: last.weight, reps: last.reps, done: false }]),
        };
      })
    );
  }

  function removeSet(setIndex) {
    setExercises((prev) =>
      prev.map((ex, exI) => {
        if (exI !== idx) return ex;
        if (ex.sets.length <= 1) return ex;
        return { ...ex, sets: ex.sets.filter((_, i) => i !== setIndex) };
      })
    );
  }

  async function endWorkout() {
    if (timerRef.current) clearInterval(timerRef.current);

    if (!userId) {
      navigate("/fitness");
      return;
    }

    const performedAt = new Date().toISOString();
    const durationMinutes = Math.max(1, Math.round(elapsedSec / 60));

    const details = {
      planId,
      sessionTitle,
      performedAt,
      durationSeconds: elapsedSec,
      exercises: exercises.map((ex) => {
        const meta = metaByNameRef.current[ex.name];
        const guide = (meta?.description || ex.notes || "").slice(0, 4000);
        return {
          name: ex.name,
          guide,
          image: (meta?.image || "").slice(0, 800),
          sets: ex.sets.map((s) => ({
            weight: String(s.weight || "").trim(),
            reps: String(s.reps || "").trim(),
            done: !!s.done,
          })),
        };
      }),
    };

    try {
      await apiClient.post("/api/workout-logs", {
        userId: Number(userId),
        workoutName: sessionTitle || "Workout",
        workoutType: "Workout",
        durationMinutes,
        performedAt,
        notes: planId ? `Plan ${planId} | ${sessionTitle}` : sessionTitle,
        detailsJson: JSON.stringify(details),
      });
    } catch {}

    navigate("/fitness");
  }

  const total = exercises.length || 1;

  return (
    <div className="pageShell">
      <div className="simpleTop">
        <button className="simpleBack" type="button" onClick={() => navigate("/fitness")}>
          Back
        </button>
        <div className="simpleTitle">Workout</div>
        <div style={{ width: 40, height: 40 }} />
      </div>

      <div className="pageBody">
        {loading ? <div className="wMsg">Loading workout…</div> : null}
        {err ? <div className="wErr">{err}</div> : null}

        {!loading && !err ? (
          <>
            <div className="workoutMetaTop">
              <div className="metaLeft">
                Exercise {Math.min(idx + 1, total)}/{total}
              </div>
              <div className="metaRight">
                Today’s session:<br />
                <b>{sessionTitle}</b>
              </div>
            </div>

            <div className="wSliderWrap">
              <div className="wSlider" style={{ transform: `translateX(-${idx * 100}%)` }}>
                {exercises.map((ex) => {
                  const meta = metaByName[ex.name];
                  const status = meta?.status || "idle";
                  const desc =
                    status === "loading"
                      ? "Loading guide…"
                      : cleanHtmlToText(meta?.description) || ex.notes || "No description available.";
                  const img = (meta?.image || "").trim();

                  return (
                    <div className="wSlide" key={ex.key}>
                      <div className="cardW">
                        <div className="rowTop">
                          <div>
                            <div className="labelW">{ex.name}</div>
                            <div className="smallW">{desc}</div>
                          </div>

                          <div className="imgW">
                            {img ? <img className="imgWEl" src={img} alt="" /> : null}
                          </div>
                        </div>

                        {exercises.length > 0 ? (
                          <div className="navExerciseRow">
                            <button
                              type="button"
                              className="navBtn"
                              onClick={() => setIdxSafe(idx - 1)}
                              disabled={idx === 0}
                            >
                              ◀
                            </button>
                            <button
                              type="button"
                              className="navBtn navBtnOn"
                              onClick={() => setIdxSafe(idx + 1)}
                              disabled={idx >= exercises.length - 1}
                            >
                              Next Exercise
                            </button>
                          </div>
                        ) : null}
                      </div>

                      <div className="cardW">
                        <div className="setsWrap">
                          <div className="tableHdrW">
                            <span>Set</span>
                            <span>Weight</span>
                            <span>Reps</span>
                            <span></span>
                            <span></span>
                          </div>

                          {ex.sets.map((s, i) => (
                            <div className="tableRowW" key={i}>
                              <span className="setPill">{i + 1}</span>

                              <input
                                className="pillInput"
                                value={s.weight}
                                onChange={(e) => editSet(i, "weight", e.target.value)}
                                placeholder="kg"
                                inputMode="decimal"
                              />

                              <input
                                className="pillInput"
                                value={s.reps}
                                onChange={(e) => editSet(i, "reps", e.target.value)}
                                placeholder="reps"
                                inputMode="numeric"
                              />

                              <button
                                type="button"
                                className={s.done ? "tickBtn tickOn" : "tickBtn"}
                                onClick={() => toggleDone(i)}
                                aria-pressed={s.done}
                              >
                                {s.done ? "✅" : "☐"}
                              </button>

                              <button
                                type="button"
                                className="minusBtn"
                                onClick={() => removeSet(i)}
                                aria-label="Remove set"
                                title="Remove set"
                              >
                                −
                              </button>
                            </div>
                          ))}
                        </div>

                        <button type="button" className="addSet" onClick={addSet}>
                          Add Set
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="bottomW">
              <div className="timerW">
                <div className="timerLabel">Time Spent Working Out</div>
                <div className="timerValue">{fmtTime(elapsedSec)}</div>
              </div>

              <button type="button" className="endBtn" onClick={endWorkout}>
                End Workout
              </button>
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
