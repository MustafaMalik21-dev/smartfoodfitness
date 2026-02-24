import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import "../styles/PageShell.css";
import "./Workout.css";

import { EXERCISE_CATALOG } from "../data/exerciseCatalog";

function cleanText(s) {
  return String(s || "").replace(/\s+/g, " ").trim();
}

function normName(s) {
  return cleanText(s).toLowerCase();
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

function slugifyFallback(s) {
  return normName(s)
    .replace(/&/g, " and ")
    .replace(/\+/g, " plus ")
    .replace(/\//g, " ")
    .replace(/[()]/g, " ")
    .replace(/[^a-z0-9\s-]/g, "")
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");
}

function useWorkoutTimer() {
  const [elapsedSec, setElapsedSec] = useState(0);
  const [isRunning, setIsRunning] = useState(false);

  const startEpochRef = useRef(null);
  const baseElapsedRef = useRef(0);
  const intervalRef = useRef(null);

  useEffect(() => {
    if (!isRunning) return;

    startEpochRef.current = Date.now();
    intervalRef.current = setInterval(() => {
      const delta = Math.floor((Date.now() - startEpochRef.current) / 1000);
      setElapsedSec(baseElapsedRef.current + delta);
    }, 250);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      intervalRef.current = null;
    };
  }, [isRunning]);

  function start() {
    if (elapsedSec > 0) return;
    baseElapsedRef.current = 0;
    setElapsedSec(0);
    setIsRunning(true);
  }

  function pause() {
    if (!isRunning) return;
    const delta = Math.floor((Date.now() - startEpochRef.current) / 1000);
    baseElapsedRef.current = baseElapsedRef.current + delta;
    setElapsedSec(baseElapsedRef.current);
    setIsRunning(false);
  }

  function resume() {
    if (isRunning) return;
    if (elapsedSec === 0) return;
    setIsRunning(true);
  }

  function stop() {
    if (!isRunning) return;
    const delta = Math.floor((Date.now() - startEpochRef.current) / 1000);
    baseElapsedRef.current = baseElapsedRef.current + delta;
    setElapsedSec(baseElapsedRef.current);
    setIsRunning(false);
  }

  return { elapsedSec, isRunning, start, pause, resume, stop };
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

  const { elapsedSec, isRunning, start, pause, resume, stop } = useWorkoutTimer();

  const [imgBusted, setImgBusted] = useState({});
  const [imgVersion, setImgVersion] = useState(0);

  const current = exercises[idx] || null;
  const total = exercises.length || 1;

  const hasPlan = !!planId;

  const resolvedMeta = useMemo(() => {
    const name = current?.name || "";
    const key = normName(name);

    const hit = EXERCISE_CATALOG?.[key] || null;
    const slug = cleanText(hit?.slug) || slugifyFallback(name);
    const description = cleanText(hit?.description) || cleanText(current?.notes) || "No description available.";

    const baseImg = slug ? `/exercise/${slug}.jpg` : "";
    const img = baseImg ? `${baseImg}?v=${imgVersion}` : "";

    return { key, slug, description, img };
  }, [current?.name, current?.notes, imgVersion]);

  useEffect(() => {
    if (!Array.isArray(exercises) || exercises.length === 0) return;

    const unique = Array.from(new Set(exercises.map((e) => normName(e?.name)).filter(Boolean)));

    unique.forEach((k) => {
      const hit = EXERCISE_CATALOG?.[k];
      const slug = cleanText(hit?.slug) || slugifyFallback(k);
      const url = slug ? `/exercise/${slug}.jpg` : "";
      if (!url) return;
      const img = new Image();
      img.src = `${url}?preload=1`;
    });
  }, [exercises]);

  useEffect(() => {
    if (!resolvedMeta.key) return;
    setImgBusted((m) => {
      if (m[resolvedMeta.key] !== true) return m;
      const copy = { ...m };
      delete copy[resolvedMeta.key];
      return copy;
    });
  }, [resolvedMeta.key]);

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

        if (!cancelled) setPlanId(selected || null);

        if (!selected) {
          if (!cancelled) {
            setSessionTitle("Workout");
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
            setSessionTitle("Workout");
            setExercises([]);
            setIdx(0);
          }
          return;
        }

        const raw = Array.isArray(first.exercises) ? first.exercises : [];

        const normalized = raw
          .map((x, i) => {
            const name = cleanText(x?.name);
            if (!name) return null;

            const setsCount = Number(x?.sets ?? 3);
            const reps = Number(x?.reps ?? 8);

            return {
              key: `${name}-${i}`,
              name,
              notes: cleanText(x?.notes || ""),
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
    stop();

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
        const key = normName(ex.name);
        const hit = EXERCISE_CATALOG?.[key] || null;
        const slug = cleanText(hit?.slug) || slugifyFallback(ex.name);
        const image = slug ? `/exercise/${slug}.jpg` : "";
        const guide = (cleanText(hit?.description) || cleanText(ex.notes) || "").slice(0, 4000);

        return {
          name: ex.name,
          guide,
          image: String(image || "").slice(0, 800),
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

  const showStart = elapsedSec === 0 && !isRunning;
  const showPause = elapsedSec > 0 && isRunning;
  const showResume = elapsedSec > 0 && !isRunning;

  const shouldShowImg = !!resolvedMeta.img && imgBusted[resolvedMeta.key] !== true;

  return (
    <div className="pageShell">
      <div className="simpleTop">
        <button className="simpleBack" type="button" onClick={() => navigate("/fitness")}>
          Back
        </button>
        <div className="simpleTitle">Workout</div>
        <div style={{ width: 40, height: 40 }} />
      </div>

      <div className="pageBody workoutBody">
        {loading ? <div className="wMsg">Loading workout…</div> : null}
        {err ? <div className="wErr">{err}</div> : null}

        {!loading && !err && !hasPlan ? (
          <div className="wEmptyCard">
            <div className="wEmptyTitle">No workout plan selected</div>
            <div className="wEmptyText">
              You haven’t selected a workout plan yet. Select one to start a workout.
            </div>
            <button className="wEmptyBtn" type="button" onClick={() => navigate("/workout-plans")}>
              Select a workout plan
            </button>
          </div>
        ) : null}

        {!loading && !err && hasPlan && current ? (
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

            <div className="workoutContent">
              <div className="cardW">
                <div className="rowTop">
                  <div className="wTextCol">
                    <div className="labelW">{current.name}</div>
                    <div className="smallW">{resolvedMeta.description}</div>
                  </div>

                  <div className="imgW">
                    {shouldShowImg ? (
                      <img
                        className="imgWEl"
                        src={resolvedMeta.img}
                        alt=""
                        loading="eager"
                        decoding="async"
                        onError={() => {
                          setImgBusted((m) => ({ ...m, [resolvedMeta.key]: true }));
                          setTimeout(() => setImgVersion((v) => v + 1), 250);
                        }}
                      />
                    ) : (
                      <div className="imgWPh">No image</div>
                    )}
                  </div>
                </div>

                <div className="navExerciseRow">
                  <button type="button" className="navBtn" onClick={() => setIdxSafe(idx - 1)} disabled={idx === 0}>
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

                  {current.sets.map((s, i) => (
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

            <div className="bottomW">
              <div className="timerW">
                <div className="timerLabel">Time Spent Working Out</div>
                <div className="timerValue">{fmtTime(elapsedSec)}</div>

                <div className="timerBtns">
                  {showStart ? (
                    <button type="button" className="timerBtn timerBtnPrimary" onClick={start}>
                      Start
                    </button>
                  ) : null}

                  {showPause ? (
                    <button type="button" className="timerBtn" onClick={pause}>
                      Pause
                    </button>
                  ) : null}

                  {showResume ? (
                    <button type="button" className="timerBtn timerBtnPrimary" onClick={resume}>
                      Resume
                    </button>
                  ) : null}
                </div>
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