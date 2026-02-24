import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import "./Profile.css";
import { getUserId } from "../auth/authStorage";

const SETTINGS_KEY = "sff_settings_v1";

function safeParse(json) { // Helper function that attempts to parse a JSON string and returns the resulting object, but if parsing fails due to invalid JSON, it catches the error and returns null instead, providing a safe way to handle potentially malformed JSON data without crashing the application
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

function unitLabel(u) { // Helper function that takes a unit string and returns a standardized label for that unit, handling common variations and providing fallbacks for unrecognized units, ensuring that the display of units in the profile page is consistent and user-friendly
  if (!u) return "";
  const x = String(u).toLowerCase();
  if (x === "kg") return "Kg";
  if (x === "lbs" || x === "lb") return "Lbs";
  if (x === "ft") return "Ft";
  if (x === "m") return "M";
  return u;
}

function formatHeight(heightValue, heightUnit) { // Function to format a height value and unit into a human-readable string, handling both feet/inches and meters, and providing fallbacks for invalid or missing values to ensure a consistent display of height information in the profile page
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

function formatHeightWithPreference(heightValue, storedUnit, preferredUnit) { // Function to format a height value based on the user's preferred unit, converting between feet and meters as needed, and providing fallbacks for invalid or missing values to ensure that the height is displayed in the user's preferred unit while maintaining a consistent and user-friendly format in the profile page
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

function aimColorClass(color) { // Helper function that takes a color string and returns a corresponding CSS class name for styling the aim chips in the profile page, handling a predefined set of color options and providing a default class for unrecognized colors, allowing for consistent and visually distinct styling of different aims based on their associated colors
  const c = (color || "").toLowerCase();
  if (c === "red") return "aimChipRed";
  if (c === "green") return "aimChipGreen";
  if (c === "yellow") return "aimChipYellow";
  if (c === "blue") return "aimChipBlue";
  if (c === "purple") return "aimChipPurple";
  return "aimChipBlue";
}

const ALL_AIMS = [ // A predefined list of possible aims that users can select for their profile, each with a unique key, a user-friendly label, and an associated color for display purposes, allowing users to choose from a variety of common fitness and health goals to personalize their profile and track their progress towards those goals in the application
  { key: "lose_weight", label: "Lose Weight", color: "red" },
  { key: "gain_muscle", label: "Gain Muscle", color: "purple" },
  { key: "get_fitter", label: "Get Fitter", color: "blue" },
  { key: "build_strength", label: "Build Strength", color: "yellow" },
  { key: "increase_steps", label: "Increase Steps", color: "green" },
  { key: "better_sleep", label: "Better Sleep", color: "purple" },
  { key: "more_energy", label: "More Energy", color: "yellow" },
  { key: "eat_healthier", label: "Eat Healthier", color: "green" },
  { key: "drink_more_water", label: "Drink More Water", color: "blue" },
  { key: "reduce_sugar", label: "Reduce Sugar", color: "red" },
  { key: "reduce_snacking", label: "Reduce Snacking", color: "red" },
  { key: "meal_prep", label: "Meal Prep", color: "green" },
  { key: "track_calories", label: "Track Calories", color: "blue" },
  { key: "hit_protein", label: "Hit Protein Goal", color: "purple" },
  { key: "balanced_macros", label: "Balanced Macros", color: "yellow" },
  { key: "run_5k", label: "Run a 5K", color: "blue" },
  { key: "run_10k", label: "Run a 10K", color: "blue" },
  { key: "cycle_more", label: "Cycle More", color: "green" },
  { key: "swim_more", label: "Swim More", color: "green" },
  { key: "stretch_daily", label: "Stretch Daily", color: "yellow" },
  { key: "improve_mobility", label: "Improve Mobility", color: "yellow" },
  { key: "better_posture", label: "Better Posture", color: "yellow" },
  { key: "reduce_stress", label: "Reduce Stress", color: "purple" },
  { key: "mindful_eating", label: "Mindful Eating", color: "green" },
  { key: "cook_more", label: "Cook More", color: "green" },
  { key: "avoid_takeaway", label: "Avoid Takeaways", color: "red" },
  { key: "consistency", label: "Be Consistent", color: "blue" },
  { key: "weekly_workouts", label: "3 Workouts/Week", color: "blue" },
  { key: "increase_flex", label: "Increase Flexibility", color: "yellow" },
  { key: "improve_health", label: "Improve Health", color: "green" },
];

export default function Profile() { // Main component for the user profile page, responsible for displaying and allowing editing of user information
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

  const [aims, setAims] = useState([]);
  const [aimsOpen, setAimsOpen] = useState(false);
  const [aimsSaving, setAimsSaving] = useState(false);
  const [aimsErr, setAimsErr] = useState("");

  const showAvatarHint = !photoDataUrl;

  useEffect(() => { // Effect hook that runs on component mount to set up an interval that checks for changes in the user's height unit preference stored in localStorage, updating the heightPref state accordingly to ensure that the displayed height information is always in sync with the user's preferred unit, and cleaning up the interval on component unmount to prevent memory leaks
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

      const fromProfile = Array.isArray(pRes.data?.aims) ? pRes.data.aims : [];
      setAims(fromProfile);

      setIsEditing(false);
      setDraft(null);
    } catch {
      setProfile(null);
      setStreak(null);
      setLatestWeight(null);
      setIsEditing(false);
      setDraft(null);
      setAims([]);
      setErrorMsg("Could not load profile.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!userId) return;
    loadAll();
  }, [userId]);

  useEffect(() => { // Effect hook that runs whenever the photoDataUrl state changes, saving the current profile picture data URL to localStorage under a key specific to the user, ensuring that the user's profile picture persists across sessions and is loaded correctly when the profile page is accessed in the future
    localStorage.setItem(PROFILE_PIC_KEY, photoDataUrl || "");
  }, [PROFILE_PIC_KEY, photoDataUrl]);

  function startEdit() {
    if (!profile) return;

    const lwVal = latestWeight?.weightValue ?? null;
    const lwUnit = latestWeight?.weightUnit ?? null;

    setDraft({
      displayName: profile.displayName ?? "",
      age: profile.age ?? "",
      gender: (profile.gender ?? "").toString(),
      activityLevel: (profile.activityLevel ?? "").toString(),
      heightValue: profile.heightValue ?? "",
      heightUnit: normalizeUnit(profile.heightUnit, "ft"),
      weightValue: lwVal ?? profile.weightValue ?? "",
      weightUnit: normalizeUnit(lwUnit ?? profile.weightUnit, "kg"),
      experienceLevel: (profile.experienceLevel ?? "").toString(),
    });

    setIsEditing(true);
    setErrorMsg("");
  }

  function cancelEdit() {
    setIsEditing(false);
    setDraft(null);
    setErrorMsg("");
  }

  async function saveEdit() { // Function to save the edited profile information, validating the inputs and preparing the payload for the API request, including handling changes to the user's weight by creating a new weight entry if the weight has changed, and then sending a PUT request to update the user profile, while managing loading state and errors to provide feedback to the user during the save process, and finally reloading the profile data to reflect any changes made
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

      const fromProfile = Array.isArray(res.data?.aims) ? res.data.aims : aims;
      setAims(fromProfile);

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

  async function saveAims(nextAims) {
    setAimsSaving(true);
    setAimsErr("");
    try {
      const res = await apiClient.put(`/api/user-profile/${userId}/aims`, {
        aims: nextAims,
      });
      const savedAims = Array.isArray(res.data?.aims) ? res.data.aims : nextAims;
      setAims(savedAims);
    } catch {
      setAimsErr("Could not save aims. Check backend validation / endpoint path.");
    } finally {
      setAimsSaving(false);
    }
  }

  function toggleAim(label) {
    const exists = aims.includes(label);
    let next = aims;

    if (exists) {
      next = aims.filter((x) => x !== label);
    } else {
      if (aims.length >= 9) return;
      next = [...aims, label];
    }

    setAims(next);
    saveAims(next);
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

  const view = useMemo(() => { // Memoized calculation of the display values for the profile information, including handling of missing or invalid data by providing fallbacks, and formatting of weight and height values based on the latest weight entry and user preferences, to ensure that the profile page displays consistent and user-friendly information about the user's profile and fitness streaks
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

  const aimMeta = useMemo(() => {
    const m = new Map();
    ALL_AIMS.forEach((a) => m.set(a.label, a));
    return m;
  }, []);

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

          {showAvatarHint ? <div className="profileAvatarHint">Click above to add profile picture</div> : null}

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
              <select
                className="pSelect"
                value={draft?.experienceLevel ?? ""}
                onChange={(e) => setDraft((d) => ({ ...d, experienceLevel: e.target.value }))}
              >
                <option value="">—</option>
                <option value="Beginner">Beginner</option>
                <option value="Intermediate">Intermediate</option>
                <option value="Advanced">Advanced</option>
              </select>
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
              <select className="pSelect" value={draft?.gender ?? ""} onChange={(e) => setDraft((d) => ({ ...d, gender: e.target.value }))}>
                <option value="">—</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
              </select>
            )}
          </div>

          <div className="pTile">
            <div className="pTileLabel">Activity Level</div>
            {!isEditing ? (
              <div className="pTileValue">{loading ? "…" : view.activityLevel}</div>
            ) : (
              <select
                className="pSelect"
                value={draft?.activityLevel ?? ""}
                onChange={(e) => setDraft((d) => ({ ...d, activityLevel: e.target.value }))}
              >
                <option value="">—</option>
                <option value="Low">Low</option>
                <option value="Moderate">Moderate</option>
                <option value="High">High</option>
              </select>
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
          <div className="profileGoalsTitleRow">
            <div className="profileGoalsTitle">Aims</div>
            <button className="aimsAddBtn" type="button" onClick={() => setAimsOpen((v) => !v)} aria-label="Choose aims">
              +
            </button>
          </div>

          {aims.length === 0 ? (
            <div className="aimsEmpty">Add up to 9 aims</div>
          ) : (
            <div className="profileGoalsChips">
              {aims.map((label) => {
                const meta = aimMeta.get(label);
                const c = aimColorClass(meta?.color || "blue");
                return (
                  <span key={label} className={`goalChip ${c}`}>
                    {label}
                  </span>
                );
              })}
            </div>
          )}

          {aimsOpen ? (
            <div className="aimsPickerInline">
              <div className="aimsPickerTop">
                <div className="aimsPickerTitle">Choose Aims ({aims.length}/9)</div>
                <button className="aimsPickerClose" type="button" onClick={() => setAimsOpen(false)}>
                  Done
                </button>
              </div>

              <div className="aimsInlineList">
                {ALL_AIMS.map((a) => {
                  const selected = aims.includes(a.label);
                  const c = aimColorClass(a.color);
                  return (
                    <button
                      key={a.key}
                      type="button"
                      className={`aimPick ${selected ? "aimPickOn" : ""}`}
                      onClick={() => toggleAim(a.label)}
                      disabled={!selected && aims.length >= 9}
                    >
                      <span className={`aimDot ${c}`} />
                      <span className="aimPickText">{a.label}</span>
                    </button>
                  );
                })}
              </div>

              {aimsErr ? <div className="aimsErr">{aimsErr}</div> : null}

              <div className="aimsPickerFooter">
                <div className="aimsPickerHint">
                  {aimsSaving ? "Saving..." : aims.length >= 9 ? "Max selected" : "Tap to select / unselect"}
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
