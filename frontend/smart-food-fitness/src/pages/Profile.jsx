import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import "./Profile.css";
import { getUserId } from "../auth/authStorage";

const SETTINGS_KEY = "sff_settings_v1";

function safeParse(json) {
  try {
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function loadHeightPref() {
  const raw = localStorage.getItem(SETTINGS_KEY);
  const parsed = safeParse(raw);
  return (parsed?.units?.height || "ft").toString().toLowerCase();
}

function toNumOrNull(v) {
  if (v === "" || v === null || v === undefined) return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function unitLabel(u) {
  if (!u) return "";
  const x = String(u).toLowerCase();
  if (x === "kg") return "Kg";
  if (x === "lbs" || x === "lb") return "Lbs";
  if (x === "ft") return "Ft";
  if (x === "m") return "M";
  return u;
}

function formatHeight(heightValue, heightUnit) {
  const v = Number(heightValue);
  const u = String(heightUnit || "").toLowerCase();
  if (!Number.isFinite(v)) return "—";

  if (u === "ft") {
    const feet = Math.floor(v);
    let inches = Math.round((v - feet) * 12);
    let f = feet;
    if (inches === 12) {
      f = feet + 1;
      inches = 0;
    }
    return `${f}'${inches}"`;
  }

  if (u === "m") {
    return `${v.toFixed(2)} m`;
  }

  return `${v} ${unitLabel(u)}`;
}

function formatHeightWithPreference(heightValue, storedUnit, preferredUnit) {
  const v = Number(heightValue);
  const from = (storedUnit || "ft").toString().toLowerCase();
  const to = (preferredUnit || "ft").toString().toLowerCase();

  if (!Number.isFinite(v)) return "—";
  if (from === to) return formatHeight(v, from);

  if (from === "ft" && to === "m") {
    const meters = v * 0.3048;
    return `${meters.toFixed(2)} m`;
  }

  if (from === "m" && to === "ft") {
    const feet = v / 0.3048;
    return formatHeight(feet, "ft");
  }

  return formatHeight(v, from);
}

function normalizeUnit(u, fallback) {
  const x = (u ?? fallback ?? "").toString().trim().toLowerCase();
  return x || (fallback ?? "");
}


export default function Profile() {
  const navigate = useNavigate();
  
  const userId = getUserId();
  const PROFILE_PIC_KEY = useMemo(() => `sff_profile_pic_user_${userId}`, [userId]);

  const [profile, setProfile] = useState(null);
  const [streak, setStreak] = useState(null);
  const [latestWeight, setLatestWeight] = useState(null);

  const [draft, setDraft] = useState(null);
  const [isEditing, setIsEditing] = useState(false);

  const [photoDataUrl, setPhotoDataUrl] = useState(() => localStorage.getItem(PROFILE_PIC_KEY) || "");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const [heightPref, setHeightPref] = useState(() => loadHeightPref());

  useEffect(() => {
    const t = setInterval(() => {
      const latest = loadHeightPref();
      setHeightPref((prev) => (prev === latest ? prev : latest));
    }, 300);

    return () => clearInterval(t);
  }, []);

  async function loadAll() {
    try {
      setErrorMsg("");
      setLoading(true);

      const [pRes, sRes, wRes] = await Promise.all([
        apiClient.get(`/api/user-profile/${userId}`),
        apiClient.get(`/api/workout-logs/user/${userId}/streak`, {
          params: { timezone: "Europe/London" },
        }),
        apiClient
          .get(`/api/weight-entries/user/${userId}/latest`)
          .then((r) => r)
          .catch(() => ({ data: null })),
      ]);

      setProfile(pRes.data);
      setStreak(sRes.data);
      setLatestWeight(wRes.data);

      setIsEditing(false);
      setDraft(null);
    } catch {
      setProfile(null);
      setStreak(null);
      setLatestWeight(null);
      setIsEditing(false);
      setDraft(null);
      setErrorMsg("Could not load profile.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!userId) return;
    loadAll();
  }, [userId]);

  useEffect(() => {
    localStorage.setItem(PROFILE_PIC_KEY, photoDataUrl || "");
  }, [PROFILE_PIC_KEY, photoDataUrl]);

  function startEdit() {
    if (!profile) return;

    const lwVal = latestWeight?.weightValue ?? null;
    const lwUnit = latestWeight?.weightUnit ?? null;

    setDraft({
      displayName: profile.displayName ?? "",
      age: profile.age ?? "",
      gender: profile.gender ?? "",
      activityLevel: profile.activityLevel ?? "",
      heightValue: profile.heightValue ?? "",
      heightUnit: normalizeUnit(profile.heightUnit, "ft"),
      weightValue: lwVal ?? profile.weightValue ?? "",
      weightUnit: normalizeUnit(lwUnit ?? profile.weightUnit, "kg"),
      experienceLevel: profile.experienceLevel ?? "",
    });

    setIsEditing(true);
    setErrorMsg("");
  }

  function cancelEdit() {
    setIsEditing(false);
    setDraft(null);
    setErrorMsg("");
  }

  async function saveEdit() {
    if (!draft) return;

    const draftWeightValue = toNumOrNull(draft.weightValue);
    const draftWeightUnit = normalizeUnit(draft.weightUnit, "kg");

    const latestWeightValue = latestWeight?.weightValue ?? null;
    const latestWeightUnit = normalizeUnit(latestWeight?.weightUnit, "");

    const weightChanged =
      draftWeightValue !== null &&
      (latestWeightValue === null ||
        Number(draftWeightValue) !== Number(latestWeightValue) ||
        draftWeightUnit !== latestWeightUnit);

    const profilePayload = {
      displayName: draft.displayName,
      age: toNumOrNull(draft.age),
      gender: draft.gender || null,
      activityLevel: draft.activityLevel || null,
      heightValue: toNumOrNull(draft.heightValue),
      heightUnit: draft.heightUnit || null,
      weightValue: draftWeightValue,
      weightUnit: draftWeightUnit,
      experienceLevel: draft.experienceLevel || null,
    };

    try {
      setSaving(true);
      setErrorMsg("");

      if (weightChanged) {
        await apiClient.post("/api/weight-entries", {
          userId,
          weightValue: draftWeightValue,
          weightUnit: draftWeightUnit,
          recordedAt: new Date().toISOString(),
        });
      }

      const res = await apiClient.put(`/api/user-profile/${userId}`, profilePayload);
      setProfile(res.data);

      setIsEditing(false);
      setDraft(null);

      try {
        const [sRes, wRes] = await Promise.all([
          apiClient.get(`/api/workout-logs/user/${userId}/streak`, {
            params: { timezone: "Europe/London" },
          }),
          apiClient
            .get(`/api/weight-entries/user/${userId}/latest`)
            .then((r) => r)
            .catch(() => ({ data: null })),
        ]);

        setStreak(sRes.data);
        setLatestWeight(wRes.data);
      } catch {
        // ignore
      }
    } catch {
      setErrorMsg("Update failed. Check backend validation / logs.");
    } finally {
      setSaving(false);
    }
  }

  function onPickPhoto(e) {
    const file = e.target.files && e.target.files[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      setErrorMsg("Please select an image file.");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      setPhotoDataUrl(String(reader.result || ""));
      setErrorMsg("");
    };
    reader.readAsDataURL(file);
  }

  function onBack() {
    if (isEditing) {
      cancelEdit();
      return;
    }
    navigate(-1);
  }

  const view = useMemo(() => {
    const p = profile || {};
    const s = streak || {};

    const age = p.age ?? "—";
    const gender = p.gender ?? "—";
    const activityLevel = p.activityLevel ?? "—";
    const experienceLevel = p.experienceLevel ?? "—";

    const wVal = latestWeight?.weightValue ?? p.weightValue ?? null;
    const wUnit = latestWeight?.weightUnit ?? p.weightUnit ?? "";
    const weightPretty =
      wVal === null || wVal === undefined || wVal === ""
        ? "—"
        : `${wVal}${wUnit ? ` ${unitLabel(wUnit)}` : ""}`;

    const heightPretty = formatHeightWithPreference(p.heightValue, p.heightUnit, heightPref);

    const currentStreakDays = s.currentStreakDays ?? "—";
    const totalWorkouts = s.totalWorkouts ?? "—";

    return {
      age,
      gender,
      activityLevel,
      experienceLevel,
      weightPretty,
      heightPretty,
      totalWorkouts,
      currentStreakDays,
    };
  }, [profile, streak, latestWeight, heightPref]);

  return (
    <div className="pageShell">
      <div className="profileTopBar">
        <button className="profileTopBtn" type="button" onClick={onBack}>
          {isEditing ? "Cancel" : "Back"}
        </button>

        <div className="profileTitle">Profile Page</div>

        {!isEditing ? (
          <button className="profileTopBtn" type="button" onClick={startEdit} disabled={loading || !profile}>
            Edit Profile
          </button>
        ) : (
          <button className="profileTopBtn" type="button" onClick={saveEdit} disabled={saving}>
            Save
          </button>
        )}
      </div>

      <div className="pageBody profileBody">
        <div className="profileAvatarBlock">
          <label className="profileAvatarCircle" title="Upload profile photo">
            {photoDataUrl ? (
              <img className="profileAvatarImg" src={photoDataUrl} alt="Profile" />
            ) : (
              <div className="profileAvatarPlaceholder">👤</div>
            )}
            <input type="file" accept="image/*" onChange={onPickPhoto} style={{ display: "none" }} />
          </label>

          {errorMsg ? <div className="profileError">{errorMsg}</div> : null}
        </div>

        <div className="profileGrid">
          <div className="pTile">
            <div className="pTileLabel">Age</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.age}</div>
            ) : (
              <input className="pInput" value={draft?.age ?? ""} onChange={(e) => setDraft((d) => ({ ...d, age: e.target.value }))} />
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Experience Level</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.experienceLevel}</div>
            ) : (
              <input
                className="pInput"
                value={draft?.experienceLevel ?? ""}
                onChange={(e) => setDraft((d) => ({ ...d, experienceLevel: e.target.value }))}
              />
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Current Weight</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.weightPretty}</div>
            ) : (
              <div className="pRow2">
                <input
                  className="pInput"
                  value={draft?.weightValue ?? ""}
                  onChange={(e) => setDraft((d) => ({ ...d, weightValue: e.target.value }))}
                />
                <select className="pSelect" value={draft?.weightUnit ?? "kg"} onChange={(e) => setDraft((d) => ({ ...d, weightUnit: e.target.value }))}>
                  <option value="kg">Kg</option>
                  <option value="lbs">Lbs</option>
                </select>
              </div>
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Highest Streak</div>
            <div className="pTileValue">{loading ? "…" : `${view.currentStreakDays} Days`}</div>
          </div>

          <div className="pTile">
            <div className="pTileLabel">Workouts Completed</div>
            <div className="pTileValue">{loading ? "…" : view.totalWorkouts}</div>
          </div>

          <div className="pTile">
            <div className="pTileLabel">Gender</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.gender}</div>
            ) : (
              <input className="pInput" value={draft?.gender ?? ""} onChange={(e) => setDraft((d) => ({ ...d, gender: e.target.value }))} />
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Activity Level</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.activityLevel}</div>
            ) : (
              <input
                className="pInput"
                value={draft?.activityLevel ?? ""}
                onChange={(e) => setDraft((d) => ({ ...d, activityLevel: e.target.value }))}
              />
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Height</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.heightPretty}</div>
            ) : (
              <div className="pRow2">
                <input
                  className="pInput"
                  value={draft?.heightValue ?? ""}
                  onChange={(e) => setDraft((d) => ({ ...d, heightValue: e.target.value }))}
                />
                <select className="pSelect" value={draft?.heightUnit ?? "ft"} onChange={(e) => setDraft((d) => ({ ...d, heightUnit: e.target.value }))}>
                  <option value="ft">Ft</option>
                  <option value="m">M</option>
                </select>
              </div>
            )}
          </div>
        </div>

        <div className="profileGoalsCard">
          <div className="profileGoalsTitle">Goals</div>
          <div className="profileGoalsChips">
            <span className="goalChip goalChipRed">Get Fit</span>
            <span className="goalChip goalChipRed">Lose Weight</span>
            <span className="goalChip goalChipGreen">Healthier</span>
            <span className="goalChip goalChipYellow">Strength</span>
            <span className="goalChip goalChipGreen">Fitness</span>
            <span className="goalChip goalChipYellow">Steps</span>
          </div>
        </div>
      </div>
    </div>
  );
}
