import { useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient";
import { useAuth } from "../../auth/useAuth";
import "./Auth.css";
import "./OnboardingGuide.css";

/* Import your custom icons */
import HomeIcon from "../../assets/Homeicon.png";
import DumbellIcon from "../../assets/Dumbellicon.png";
import TrackingIcon from "../../assets/Trackingicon.png";
import FoodIcon from "../../assets/Foodicon.png";
import SettingsIcon from "../../assets/Settingsicon.png";
import PfpIcon from "../../assets/PFP.png";

const GUIDE_ITEMS = [
  {
    key: "dashboard",
    title: "Dashboard",
    icon: HomeIcon,
    text: "Summary of calories and macros, plus quick access to what you use most.",
    tip: "Recommended: check your targets and today’s progress first.",
  },
  {
    key: "fitness",
    title: "Fitness",
    icon: DumbellIcon,
    text: "Access page for all fitness related features including workouts and plans.",
    tip: "Recommended: open Workout Plans and pick a plan to follow.",
  },
  {
    key: "tracking",
    title: "Tracking",
    icon: TrackingIcon,
    text: "Shows detailed tracking of both food and fitness progress.",
    tip: "Recommended: review trends weekly to stay consistent.",
  },
  {
    key: "food",
    title: "Food",
    icon: FoodIcon,
    text: "Log meals, check calories, and explore recipes.",
    tip: "Recommended: try the Recipes page for quick meal ideas.",
  },
  {
    key: "settings",
    title: "Settings",
    icon: SettingsIcon,
    text: "Customise your experience and app preferences.",
    tip: "Recommended: enable Dark Mode for a nicer look.",
  },
  {
    key: "profile",
    title: "Profile",
    icon: PfpIcon,
    text: "View and manage all your personal data and aims.",
    tip: "Recommended: keep your details updated for better accuracy.",
  },
];

export default function OnboardingGuide() {
  const navigate = useNavigate();
  const { auth, setAuth } = useAuth();

  const userId = auth && typeof auth.userId !== "undefined" ? auth.userId : null;

  const [busy, setBusy] = useState(false);
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

  async function completeGuide() {
    if (busy) return;

    setBusy(true);
    setErr("");

    try {
      await apiClient.put(`/api/user-profile/${userId}`, {
        email: auth && auth.email ? auth.email : null,
        displayName: auth && auth.displayName ? auth.displayName : null,
        onboardingComplete: true,
      });

      setAuth({
        userId: auth.userId,
        email: auth.email,
        displayName: auth.displayName,
        token: auth.token,
        onboardingComplete: true,
      });

      navigate("/", { replace: true });
    } catch (e) {
      const msg =
        (e && e.response && e.response.data && e.response.data.message) ||
        (e && e.response && e.response.data) ||
        "Could not finish setup. Please try again.";
      setErr(String(msg));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="authScreen">
      <div className="authTopBar">
        <div className="authAppTitle">Smart Food &amp; Fitness</div>
      </div>

      <div className="authBody">
        <div className="authPanel">
          <h1 className="authH1">Quick guide</h1>
          <p className="authSub">Here’s what each page does so navigation feels easier.</p>

          {err ? <div className="authErr">{err}</div> : null}

          <div className="ogList">
            {GUIDE_ITEMS.map((x) => (
              <div className="ogCard" key={x.key}>
                <div className="ogIcon">
                  <img src={x.icon} alt="" className="ogIconImg" />
                </div>
                <div className="ogText">
                  <div className="ogTitle">{x.title}</div>
                  <div className="ogDesc">{x.text}</div>
                  <div className="ogTip">{x.tip}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="ogBtnsSingle">
            <button className="authBtn ogBtn" type="button" onClick={completeGuide} disabled={busy}>
              {busy ? "Saving…" : "Start using the app"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
