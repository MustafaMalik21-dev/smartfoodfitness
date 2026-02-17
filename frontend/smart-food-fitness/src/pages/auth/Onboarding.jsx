import { useState } from "react";
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

export default function Onboarding() {
  const navigate = useNavigate();
  const { auth, setAuth } = useAuth();

  const userId = auth && typeof auth.userId !== "undefined" ? auth.userId : null;

  const [step, setStep] = useState(1);

  const [age, setAge] = useState("");
  const [gender, setGender] = useState("");
  const [activityLevel, setActivityLevel] = useState("");
  const [experienceLevel, setExperienceLevel] = useState("");

  const [heightValue, setHeightValue] = useState("");
  const [heightUnit, setHeightUnit] = useState("ft");
  const [weightValue, setWeightValue] = useState("");
  const [weightUnit, setWeightUnit] = useState("kg");

  const [calorieGoal, setCalorieGoal] = useState("2000");
  const [proteinGoal, setProteinGoal] = useState("150");
  const [carbGoal, setCarbGoal] = useState("200");
  const [fatGoal, setFatGoal] = useState("60");

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

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

    try {
      setLoading(true);

      await apiClient.put(`/api/user-profile/${userId}`, {
        email: auth && auth.email ? auth.email : null,
        displayName: auth && auth.displayName ? auth.displayName : null,

        age: toNum(age),
        gender: toLowerOrNull(gender),
        activityLevel: toLowerOrNull(activityLevel),
        experienceLevel: toLowerOrNull(experienceLevel),

        heightValue: toNum(heightValue),
        heightUnit: String(heightUnit || "ft").toLowerCase(),

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
          const status = e && e.response ? e.response.status : 0;
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
                <div className="authLabel">Activity level</div>
                <select className="authSelect" value={activityLevel} onChange={(e) => setActivityLevel(e.target.value)}>
                  <option value="">Select</option>
                  <option value="low">Low</option>
                  <option value="moderate">Moderate</option>
                  <option value="high">High</option>
                </select>
              </div>

              <div className="authField">
                <div className="authLabel">Experience level</div>
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
              </div>

              <button className="authBtn" type="button" onClick={() => setStep(2)}>
                Continue
              </button>
            </div>
          ) : null}

          {step === 2 ? (
            <div className="authForm">
              <div className="authRow2">
                <div className="authField">
                  <div className="authLabel">Height</div>
                  <input
                    className="authInput"
                    value={heightValue}
                    onChange={(e) => setHeightValue(e.target.value)}
                    placeholder="e.g. 5.8"
                    inputMode="decimal"
                  />
                </div>

                <div className="authField">
                  <div className="authLabel">Unit</div>
                  <select className="authSelect" value={heightUnit} onChange={(e) => setHeightUnit(e.target.value)}>
                    <option value="ft">Feet</option>
                    <option value="m">Meters</option>
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
                    placeholder="e.g. 78"
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
                <button className="authBtn" type="button" onClick={() => setStep(3)}>
                  Continue
                </button>
              </div>
            </div>
          ) : null}

          {step === 3 ? (
            <div className="authForm">
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
