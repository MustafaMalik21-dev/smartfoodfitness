import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient";
import { useAuth } from "../../auth/useAuth";
import "./Auth.css";

function toNum(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function toLowerOrNull(v) {
  const s = String(v || "").trim();
  return s ? s.toLowerCase() : null;
}

function clampInt(x, min, max) {
  const n = Number(x);
  if (!Number.isFinite(n)) return min;
  return Math.max(min, Math.min(max, Math.round(n)));
}

function round1(x) {
  const n = Number(x);
  if (!Number.isFinite(n)) return 0;
  return Math.round(n * 10) / 10;
}

function lbsToKg(lbs) {
  return Number(lbs) / 2.2046226218;
}

function kgToLbs(kg) {
  return Number(kg) * 2.2046226218;
}

function ftToCm(ft) {
  return Number(ft) * 30.48;
}

function mToCm(m) {
  return Number(m) * 100;
}

function activityMultiplier(level) {
  const v = String(level || "").toLowerCase();
  if (v === "low") return 1.2;
  if (v === "moderate") return 1.55;
  if (v === "high") return 1.725;
  return 1.2;
}

function mifflinStJeorBmr({ sex, age, heightCm, weightKg }) {
  const a = Number(age);
  const h = Number(heightCm);
  const w = Number(weightKg);
  if (!Number.isFinite(a) || !Number.isFinite(h) || !Number.isFinite(w)) return null;

  const base = 10 * w + 6.25 * h - 5 * a;
  if (sex === "male") return base + 5;
  if (sex === "female") return base - 161;
  return base - 78;
}

function deriveTargets({ weightKg, tdee, goal }) {
  const w = Number(weightKg);
  const cals = Number(tdee);
  if (!Number.isFinite(w) || !Number.isFinite(cals) || w <= 0 || cals <= 0) {
    return { calories: 2000, protein: 150, carbs: 200, fat: 60 };
  }

  let targetCalories = cals;
  const g = String(goal || "maintain").toLowerCase();
  if (g === "lose") targetCalories = cals - 300;
  if (g === "gain") targetCalories = cals + 250;

  targetCalories = Math.max(1200, Math.round(targetCalories));

  const proteinG = Math.round(w * 1.8);
  const fatG = Math.round((targetCalories * 0.25) / 9);
  const carbsG = Math.max(0, Math.round((targetCalories - proteinG * 4 - fatG * 9) / 4));

  return {
    calories: targetCalories,
    protein: proteinG,
    carbs: carbsG,
    fat: fatG,
  };
}

export default function Onboarding() {
  const navigate = useNavigate();
  const { auth, setAuth } = useAuth();

  const userId = auth && typeof auth.userId !== "undefined" ? auth.userId : null;

  const [step, setStep] = useState(1);

  const [age, setAge] = useState("");
  const [gender, setGender] = useState("");
  const [activityLevel, setActivityLevel] = useState("");
  const [experienceLevel, setExperienceLevel] = useState("");

  const [heightUnit, setHeightUnit] = useState("ft");
  const [heightFt, setHeightFt] = useState("");
  const [heightIn, setHeightIn] = useState("");
  const [heightM, setHeightM] = useState("");
  const [heightCmPart, setHeightCmPart] = useState("");

  const [weightValue, setWeightValue] = useState("");
  const [weightUnit, setWeightUnit] = useState("kg");

  const [goal, setGoal] = useState("maintain"); // maintain | lose | gain

  const [calorieGoal, setCalorieGoal] = useState("2000");
  const [proteinGoal, setProteinGoal] = useState("150");
  const [carbGoal, setCarbGoal] = useState("200");
  const [fatGoal, setFatGoal] = useState("60");

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const [showActHelp, setShowActHelp] = useState(false);
  const [showExpHelp, setShowExpHelp] = useState(false);

  const parsed = useMemo(() => {
    const a = toNum(age);
    const wv = toNum(weightValue);

    let heightCm = null;

    if (String(heightUnit).toLowerCase() === "ft") {
      const ft = toNum(heightFt);
      const inch = toNum(heightIn);

      if (ft != null && inch != null) {
        heightCm = ft * 30.48 + inch * 2.54;
      }
    } else {
      const m = toNum(heightM);
      const cm = toNum(heightCmPart);

      if (m != null && cm != null) {
        heightCm = m * 100 + cm;
      }
    }

    let weightKg = null;
    if (wv != null) {
      weightKg = String(weightUnit).toLowerCase() === "lbs" ? lbsToKg(wv) : wv;
    }

    return { a, wv, heightCm, weightKg };
  }, [age, weightValue, weightUnit, heightUnit, heightFt, heightIn, heightM, heightCmPart]);

  function validateStep1() {
    const a = parsed.a;
    if (a == null) return "Please enter your age.";
    if (a < 5 || a > 100) return "Please enter a sensible age (between 5 and 100).";
    if (!gender) return "Please select your gender.";
    if (!activityLevel) return "Please select your activity level.";
    if (!experienceLevel) return "Please select your experience level.";
    return "";
  }

  function validateStep2() {
    const unit = String(heightUnit).toLowerCase();
    const hc = parsed.heightCm;

    if (unit === "ft") {
      const ft = toNum(heightFt);
      const inch = toNum(heightIn);
      if (ft == null) return "Please enter your height in feet.";
      if (inch == null) return "Please enter your height in inches.";
      if (inch < 0 || inch > 11) return "Inches must be between 0 and 11.";
      if (hc == null) return "Please enter your height.";
      if (hc < 2 * 30.48 || hc > 8 * 30.48) return "Please enter a sensible height (between 2ft and 8ft).";
    } else {
      const m = toNum(heightM);
      const cm = toNum(heightCmPart);
      if (m == null) return "Please enter your height in meters.";
      if (cm == null) return "Please enter your height in cm.";
      if (cm < 0 || cm > 99) return "Cm must be between 0 and 99.";
      if (hc == null) return "Please enter your height.";
      if (hc < 60 || hc > 250) return "Please enter a sensible height (between 0.6m and 2.5m).";
    }

    const wv = parsed.wv;
    if (wv == null) return "Please enter your weight.";
    if (String(weightUnit).toLowerCase() === "kg") {
      if (wv < 30 || wv > 200) return "Please enter a sensible weight (between 30kg and 200kg).";
    } else {
      if (wv < 66 || wv > 440) return "Please enter a sensible weight (between 66lbs and 440lbs).";
    }

    return "";
  }

  function applyRecommendedTargets() {
    const sex = toLowerOrNull(gender);
    const a = parsed.a;
    const heightCm = parsed.heightCm;
    const weightKg = parsed.weightKg;

    if (!sex || a == null || heightCm == null || weightKg == null) return;

    const bmr = mifflinStJeorBmr({ sex, age: a, heightCm, weightKg });
    if (!bmr) return;

    const tdee = bmr * activityMultiplier(activityLevel);
    const rec = deriveTargets({ weightKg, tdee, goal });

    setCalorieGoal(String(rec.calories));
    setProteinGoal(String(rec.protein));
    setCarbGoal(String(rec.carbs));
    setFatGoal(String(rec.fat));
  }

  if (!userId) {
    return (
      <div className="authScreen">
        <div className="authTopBar">
          <div className="authAppTitle">Smart Food &amp; Fitness</div>
        </div>
        <div className="authBody">
          <div className="authPanel">
            <h1 className="authH1">You’re not logged in</h1>
            <p className="authSub">Please log in first.</p>
          </div>
        </div>
      </div>
    );
  }

  async function finish() {
    setErr("");

    const v1 = validateStep1();
    if (v1) {
      setErr(v1);
      setStep(1);
      return;
    }

    const v2 = validateStep2();
    if (v2) {
      setErr(v2);
      setStep(2);
      return;
    }

    try {
      setLoading(true);

      let heightValueToSend = null;
      let heightUnitToSend = String(heightUnit || "ft").toLowerCase();

      if (heightUnitToSend === "ft") {
        const ft = clampInt(heightFt, 0, 20);
        const inch = clampInt(heightIn, 0, 11);
        heightValueToSend = round1(ft + inch / 12);
      } else {
        const m = clampInt(heightM, 0, 3);
        const cm = clampInt(heightCmPart, 0, 99);
        heightValueToSend = round1(m + cm / 100);
      }

      await apiClient.put(`/api/user-profile/${userId}`, {
        email: auth && auth.email ? auth.email : null,
        displayName: auth && auth.displayName ? auth.displayName : null,

        age: toNum(age),
        gender: toLowerOrNull(gender),
        activityLevel: toLowerOrNull(activityLevel),
        experienceLevel: toLowerOrNull(experienceLevel),

        heightValue: heightValueToSend,
        heightUnit: heightUnitToSend,

        weightValue: toNum(weightValue),
        weightUnit: String(weightUnit || "kg").toLowerCase(),

        onboardingComplete: false,
      });

      try {
        await apiClient.post("/api/user-goals", {
          userId: userId,
          calorieGoal: Math.max(1, Number(calorieGoal)),
          proteinGoal: Math.max(1, Number(proteinGoal)),
          carbGoal: Math.max(1, Number(carbGoal)),
          fatGoal: Math.max(1, Number(fatGoal)),
        });
      } catch (e) {
        const status = e && e.response ? e.response.status : 0;
        if (status !== 409) throw e;
      }

      if (String(weightValue).trim() !== "") {
        try {
          await apiClient.post("/api/weight-entries", {
            userId: userId,
            weightValue: toNum(weightValue),
            weightUnit: String(weightUnit || "kg").toLowerCase(),
            recordedAt: new Date().toISOString(),
          });
        } catch (e) {
          const status = e && e.response ? e && e.response ? e.response.status : 0 : 0;
          if (status !== 409) throw e;
        }
      }

      setAuth({
        userId: auth.userId,
        email: auth.email,
        displayName: auth.displayName,
        token: auth.token,
        onboardingComplete: false,
      });

      navigate("/onboarding-guide", { replace: true });
    } catch (e2) {
      const msg =
        (e2 && e2.response && e2.response.data && e2.response.data.message) ||
        (e2 && e2.response && e2.response.data) ||
        "Setup failed. Check the values and try again.";
      setErr(String(msg));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="authScreen">
      <div className="authTopBar">
        <div className="authAppTitle">Smart Food &amp; Fitness</div>
      </div>

      <div className="authBody">
        <div className="authPanel">
          <h1 className="authH1">Quick setup</h1>
          <p className="authSub">This helps personalise your dashboard and targets.</p>

          <div className="authSteps" aria-hidden="true">
            <span className={step === 1 ? "authDot authDotOn" : "authDot"} />
            <span className={step === 2 ? "authDot authDotOn" : "authDot"} />
            <span className={step === 3 ? "authDot authDotOn" : "authDot"} />
          </div>

          {err ? <div className="authErr">{err}</div> : null}

          {step === 1 ? (
            <div className="authForm">
              <div className="authRow2">
                <div className="authField">
                  <div className="authLabel">Age</div>
                  <input
                    className="authInput"
                    value={age}
                    onChange={(e) => setAge(e.target.value)}
                    placeholder="e.g. 21"
                    inputMode="numeric"
                  />
                </div>

                <div className="authField">
                  <div className="authLabel">Gender</div>
                  <select className="authSelect" value={gender} onChange={(e) => setGender(e.target.value)}>
                    <option value="">Select</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                  </select>
                </div>
              </div>

              <div className="authField">
                <div className="authLabelRow">
                  <span>Activity level</span>
                  <button
                    type="button"
                    className="authHelpBtn"
                    aria-label="What is activity level?"
                    onClick={() => {
                      setShowActHelp((v) => !v);
                      setShowExpHelp(false);
                    }}
                  >
                    ?
                  </button>
                </div>
                <select className="authSelect" value={activityLevel} onChange={(e) => setActivityLevel(e.target.value)}>
                  <option value="">Select</option>
                  <option value="low">Low</option>
                  <option value="moderate">Moderate</option>
                  <option value="high">High</option>
                </select>
                {showActHelp ? (
                  <div className="authHelp">
                    <strong>Activity level</strong> = how much you move day-to-day.
                    <ul>
                      <li>
                        <strong>Low:</strong> mostly sitting, little exercise.
                      </li>
                      <li>
                        <strong>Moderate:</strong> some walking + a few workouts per week.
                      </li>
                      <li>
                        <strong>High:</strong> active job and/or frequent training (most days).
                      </li>
                    </ul>
                  </div>
                ) : null}
              </div>

              <div className="authField">
                <div className="authLabelRow">
                  <span>Experience level</span>
                  <button
                    type="button"
                    className="authHelpBtn"
                    aria-label="What is experience level?"
                    onClick={() => {
                    setShowExpHelp((v) => !v);
                    setShowActHelp(false);
                    }}
                  >
                    ?
                  </button>
                </div>
                <select
                  className="authSelect"
                  value={experienceLevel}
                  onChange={(e) => setExperienceLevel(e.target.value)}
                >
                  <option value="">Select</option>
                  <option value="beginner">Beginner</option>
                  <option value="intermediate">Intermediate</option>
                  <option value="advanced">Advanced</option>
                </select>
                {showExpHelp ? (
                  <div className="authHelp">
                    <strong>Experience level</strong> = how confident you are in the gym.
                    <ul>
                      <li>
                        <strong>Beginner:</strong> new to training, learning technique.
                      </li>
                      <li>
                        <strong>Intermediate:</strong> consistent for months, knows main lifts.
                      </li>
                      <li>
                        <strong>Advanced:</strong> long-term training, structured programming.
                      </li>
                    </ul>
                  </div>
                ) : null}
              </div>

              <button
                className="authBtn"
                type="button"
                onClick={() => {
                  const v = validateStep1();
                  if (v) return setErr(v);
                  setErr("");
                  setStep(2);
                }}
              >
                Continue
              </button>
            </div>
          ) : null}

          {step === 2 ? (
            <div className="authForm">
              <div className="authRow2">
                <div className="authField">
                  <div className="authLabel">Height</div>

                  {String(heightUnit).toLowerCase() === "ft" ? (
                    <div className="authRow2">
                      <div className="authField">
                        <div className="authLabel">Feet</div>
                        <input
                          className="authInput"
                          value={heightFt}
                          onChange={(e) => setHeightFt(e.target.value)}
                          placeholder="e.g. 5"
                          inputMode="numeric"
                        />
                      </div>
                      <div className="authField">
                        <div className="authLabel">Inches</div>
                        <input
                          className="authInput"
                          value={heightIn}
                          onChange={(e) => setHeightIn(e.target.value)}
                          placeholder="e.g. 10"
                          inputMode="numeric"
                        />
                      </div>
                    </div>
                  ) : (
                    <div className="authRow2">
                      <div className="authField">
                        <div className="authLabel">Meters</div>
                        <input
                          className="authInput"
                          value={heightM}
                          onChange={(e) => setHeightM(e.target.value)}
                          placeholder="e.g. 1"
                          inputMode="numeric"
                        />
                      </div>
                      <div className="authField">
                        <div className="authLabel">Cm</div>
                        <input
                          className="authInput"
                          value={heightCmPart}
                          onChange={(e) => setHeightCmPart(e.target.value)}
                          placeholder="e.g. 78"
                          inputMode="numeric"
                        />
                      </div>
                    </div>
                  )}
                </div>

                <div className="authField">
                  <div className="authLabel">Unit</div>
                  <select className="authSelect" value={heightUnit} onChange={(e) => setHeightUnit(e.target.value)}>
                    <option value="ft">Feet / Inches</option>
                    <option value="m">Meters / Cm</option>
                  </select>
                </div>
              </div>

              <div className="authRow2">
                <div className="authField">
                  <div className="authLabel">Current weight</div>
                  <input
                    className="authInput"
                    value={weightValue}
                    onChange={(e) => setWeightValue(e.target.value)}
                    placeholder={weightUnit === "lbs" ? "e.g. 172" : "e.g. 78"}
                    inputMode="decimal"
                  />
                </div>

                <div className="authField">
                  <div className="authLabel">Unit</div>
                  <select className="authSelect" value={weightUnit} onChange={(e) => setWeightUnit(e.target.value)}>
                    <option value="kg">Kg</option>
                    <option value="lbs">Lbs</option>
                  </select>
                </div>
              </div>

              <div className="authRow2">
                <button className="authBtnSecondary" type="button" onClick={() => setStep(1)}>
                  Back
                </button>
                <button
                  className="authBtn"
                  type="button"
                  onClick={() => {
                    const v = validateStep2();
                    if (v) return setErr(v);
                    setErr("");
                    applyRecommendedTargets();
                    setStep(3);
                  }}
                >
                  Continue
                </button>
              </div>
            </div>
          ) : null}

          {step === 3 ? (
            <div className="authForm">
              <div className="authField">
                <div className="authLabel">Goal</div>
                <select
                  className="authSelect"
                  value={goal}
                  onChange={(e) => {
                    setGoal(e.target.value);
                    setTimeout(() => applyRecommendedTargets(), 0);
                  }}
                >
                  <option value="maintain">Maintain</option>
                  <option value="lose">Lose fat</option>
                  <option value="gain">Gain muscle</option>
                </select>
              </div>

              <button
                className="authBtnSecondary"
                type="button"
                onClick={() => {
                  applyRecommendedTargets();
                }}
              >
                Recalculate recommended targets
              </button>

              <div className="authField">
                <div className="authLabel">Daily calorie goal</div>
                <input className="authInput" value={calorieGoal} onChange={(e) => setCalorieGoal(e.target.value)} />
              </div>

              <div className="authRow2">
                <div className="authField">
                  <div className="authLabel">Protein (g)</div>
                  <input className="authInput" value={proteinGoal} onChange={(e) => setProteinGoal(e.target.value)} />
                </div>
                <div className="authField">
                  <div className="authLabel">Carbs (g)</div>
                  <input className="authInput" value={carbGoal} onChange={(e) => setCarbGoal(e.target.value)} />
                </div>
              </div>

              <div className="authField">
                <div className="authLabel">Fat (g)</div>
                <input className="authInput" value={fatGoal} onChange={(e) => setFatGoal(e.target.value)} />
              </div>

              <div className="authRow2">
                <button className="authBtnSecondary" type="button" onClick={() => setStep(2)}>
                  Back
                </button>
                <button className="authBtn" type="button" disabled={loading} onClick={finish}>
                  {loading ? "Saving…" : "Finish setup"}
                </button>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
