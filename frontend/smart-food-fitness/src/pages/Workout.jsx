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

function normalizeSearchList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.results)) return data.results;
  if (Array.isArray(data?.data)) return data.data;
  return [];
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

function pickImageFromDetail(detail, fallback) {
  const imgs = Array.isArray(detail?.images) ? detail.images : [];
  const first = imgs.find(Boolean);

  const fromImages =
    typeof first === "string"
      ? first
      : typeof first?.url === "string"
      ? first.url
      : typeof first?.imageUrl === "string"
      ? first.imageUrl
      : "";

  const direct =
    (typeof detail?.imageUrl === "string" ? detail.imageUrl : "") ||
    (typeof detail?.gifUrl === "string" ? detail.gifUrl : "") ||
    (typeof detail?.thumbnailUrl === "string" ? detail.thumbnailUrl : "");

  const hitFallback =
    (typeof fallback?.imageUrl === "string" ? fallback.imageUrl : "") ||
    (typeof fallback?.gifUrl === "string" ? fallback.gifUrl : "") ||
    (typeof fallback?.thumbnailUrl === "string" ? fallback.thumbnailUrl : "");

  return String(fromImages || direct || hitFallback || "").trim();
}

function pickDescriptionFromDetail(detail) {
  const candidates = [
    detail?.description,
    detail?.instructions,
    detail?.guide,
    detail?.howTo,
    detail?.summary,
  ]
    .map((x) => (typeof x === "string" ? x : ""))
    .map(cleanHtmlToText)
    .filter(Boolean);

  return candidates[0] || "";
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

  const [metaByName, setMetaByName] = useState({});
  const metaByNameRef = useRef({});
  useEffect(() => {
    metaByNameRef.current = metaByName;
  }, [metaByName]);

  const current = exercises[idx] || null;
  const total = exercises.length || 1;

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
    const name = current?.name;
    if (!name) return;

    let cancelled = false;

    async function loadMeta() {
      const existing = metaByNameRef.current[name];
      if (existing && existing.status !== "error") return;

      setMetaByName((m) => ({
        ...m,
        [name]: { status: "loading", description: "", image: "" },
      }));

      try {
        const searchRes = await apiClient.get("/api/exercises/search", {
          params: { q: name, limit: 25, scope: "all" },
        });

        if (cancelled) return;

        const list = normalizeSearchList(searchRes.data);
        const hit = pickBestSearchHit(list, name);

        if (!hit?.id) {
          setMetaByName((m) => ({
            ...m,
            [name]: { status: "done", description: "", image: "" },
          }));
          return;
        }

        const detailRes = await apiClient.get(`/api/exercises/${hit.id}`);
        if (cancelled) return;

        const detail = detailRes?.data || {};
        const image = pickImageFromDetail(detail, hit);
        const description = pickDescriptionFromDetail(detail);

        setMetaByName((m) => ({
          ...m,
          [name]: { status: "done", description, image },
        }));
      } catch {
        setMetaByName((m) => ({
          ...m,
          [name]: { status: "error", description: "", image: "" },
        }));
      }
    }

    loadMeta();
    return () => {
      cancelled = true;
    };
  }, [current?.name]);

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
        const meta = metaByNameRef.current[ex.name];
        const guide = (meta?.description || ex.notes || "").slice(0, 4000);
        return {
          name: ex.name,
          guide,
          image: String(meta?.image || "").slice(0, 800),
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

  const meta = current?.name ? metaByName[current.name] : null;
  const status = meta?.status || "idle";
  const img = String(meta?.image || "").trim();
  const desc =
    status === "loading"
      ? "Loading guide…"
      : cleanHtmlToText(meta?.description) || current?.notes || "No description available.";

  const showStart = elapsedSec === 0 && !isRunning;
  const showPause = elapsedSec > 0 && isRunning;
  const showResume = elapsedSec > 0 && !isRunning;

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

        {!loading && !err && current ? (
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
                  <div>
                    <div className="labelW">{current.name}</div>
                    <div className="smallW">{desc}</div>
                  </div>

                  <div className="imgW">
                    {img ? <img className="imgWEl" src={img} alt="" /> : <div className="imgWPh">No image</div>}
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

                      <button type="button" className="minusBtn" onClick={() => removeSet(i)} aria-label="Remove set" title="Remove set">
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
