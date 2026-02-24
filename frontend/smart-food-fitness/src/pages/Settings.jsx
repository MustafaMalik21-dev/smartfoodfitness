import { useEffect, useMemo, useState } from "react";
import HeaderBar from "../components/HeaderBar";
import "../styles/PageShell.css";
import "./Settings.css";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

const LS_KEY = "sff_settings_v1";

const DEFAULTS = { // Default settings for the application, including units for weight and height, tracking preferences for macros, weight, and workouts, notification preferences for workouts, food, and streaks, and accessibility options for text size and mode, providing a baseline configuration that can be customized by the user and persisted in localStorage
  units: { weight: "kg", height: "ft" },
  tracking: { macros: true, weight: true, workouts: true },
  notifications: { workouts: true, food: true, streak: true },
  accessibility: { textSize: "small", mode: "light" },
};

function safeParse(json) {// Helper function to safely parse a JSON string, returning the parsed object if successful or null if parsing fails due to invalid JSON format, providing a way to handle potential errors when retrieving and parsing settings from localStorage without crashing the application
  try {
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function mergeDefaults(saved) { // Function to merge the saved settings from localStorage with the default settings, ensuring that any missing properties in the saved settings are filled in with the default values, and that the structure of the settings is maintained even if the saved data is incomplete or malformed, providing a robust way to handle user preferences while preventing issues caused by invalid data
  if (!saved || typeof saved !== "object") return DEFAULTS;
  return {
    units: { ...DEFAULTS.units, ...(saved.units || {}) },
    tracking: { ...DEFAULTS.tracking, ...(saved.tracking || {}) },
    notifications: { ...DEFAULTS.notifications, ...(saved.notifications || {}) },
    accessibility: { ...DEFAULTS.accessibility, ...(saved.accessibility || {}) },
  };
}

function PillTabs({ options, value, onChange, columns }) { // Component for rendering a set of pill-shaped tabs based on the provided options, allowing the user to select one of the options and triggering the onChange callback with the selected value, while also supporting an optional columns prop to control the layout of the tabs in a responsive grid format
  const cols = columns || options.length;

  return (
    <div
      className="sTabs"
      role="tablist"
      aria-label="tabs"
      style={{ gridTemplateColumns: `repeat(${cols}, 1fr)` }}
    >
      {options.map((opt) => {
        const active = opt.value === value;
        return (
          <button
            key={opt.value}
            type="button"
            className={active ? "sTab sTabOn" : "sTab"}
            onClick={() => onChange(opt.value)}
            aria-pressed={active}
          >
            {opt.label}
          </button>
        );
      })}
    </div>
  );
}

function ToggleRow({ label, checked, onChange }) { // Component for rendering a toggle switch with a label, allowing the user to toggle a boolean setting on or off, and triggering the onChange callback with the new value when the toggle is clicked, while also providing visual feedback on the current state of the toggle through styling and ARIA attributes for accessibility
  return (
    <div className="sRow">
      <div className="sRowLabel">{label}</div>
      <button
        type="button"
        className={checked ? "sToggle sToggleOn" : "sToggle"}
        onClick={() => onChange(!checked)}
        aria-pressed={checked}
      >
        <span className="sKnob" />
      </button>
    </div>
  );
}

export default function Settings() { // Main component for the settings page, responsible for managing user preferences and allowing the user to customize their experience with the application, including handling of units, tracking options, notifications, and accessibility settings, while also providing a logout button to allow the user to sign out of their account
  const [settings, setSettings] = useState(DEFAULTS);
  const [hasLoaded, setHasLoaded] = useState(false);
  const navigate = useNavigate();
  const { logout } = useAuth();

  useEffect(() => {
    const raw = localStorage.getItem(LS_KEY);
    const parsed = safeParse(raw);
    setSettings(mergeDefaults(parsed));
    setHasLoaded(true);
  }, []);

  useEffect(() => {
    if (!hasLoaded) return;
    localStorage.setItem(LS_KEY, JSON.stringify(settings));
  }, [settings, hasLoaded]);

  const textSize = settings.accessibility.textSize;
  const mode = settings.accessibility.mode;

  useEffect(() => {
    document.documentElement.dataset.textsize = textSize;
    document.documentElement.dataset.mode = mode;
  }, [textSize, mode]);

  const prefs = useMemo(
    () => ({
      weight: settings.units.weight,
      height: settings.units.height,
    }),
    [settings.units.weight, settings.units.height]
  );

  const setUnit = (k, v) => setSettings((s) => ({ ...s, units: { ...s.units, [k]: v } }));
  const setTracking = (k, v) => setSettings((s) => ({ ...s, tracking: { ...s.tracking, [k]: v } }));
  const setNotifications = (k, v) =>
    setSettings((s) => ({ ...s, notifications: { ...s.notifications, [k]: v } }));
  const setAccessibility = (k, v) =>
    setSettings((s) => ({ ...s, accessibility: { ...s.accessibility, [k]: v } }));

  return (
    <div className="pageShell settingsShell">
      <HeaderBar title="Settings" />

      <div className="pageBody settingsBody">
        <div className="sCard">
          <div className="sCardTitle">Preferences</div>

          <div className="sPrefRow">
            <div className="sPrefLabel">Weight</div>
            <PillTabs
              value={prefs.weight}
              onChange={(v) => setUnit("weight", v)}
              columns={2}
              options={[
                { label: "Kg", value: "kg" },
                { label: "Lbs", value: "lbs" },
              ]}
            />
          </div>

          <div className="sPrefRow">
            <div className="sPrefLabel">Height</div>
            <PillTabs
              value={prefs.height}
              onChange={(v) => setUnit("height", v)}
              columns={2}
              options={[
                { label: "Ft", value: "ft" },
                { label: "M", value: "m" },
              ]}
            />
          </div>

          <div className="sSectionLabel">Tracking</div>
          <ToggleRow label="Macros" checked={settings.tracking.macros} onChange={(v) => setTracking("macros", v)} />
          <ToggleRow label="Weight" checked={settings.tracking.weight} onChange={(v) => setTracking("weight", v)} />
          <ToggleRow
            label="Workouts"
            checked={settings.tracking.workouts}
            onChange={(v) => setTracking("workouts", v)}
          />
        </div>

        <div className="sCard">
          <div className="sCardTitle">Notifications</div>
          <ToggleRow
            label="Workouts"
            checked={settings.notifications.workouts}
            onChange={(v) => setNotifications("workouts", v)}
          />
          <ToggleRow
            label="Food"
            checked={settings.notifications.food}
            onChange={(v) => setNotifications("food", v)}
          />
          <ToggleRow
            label="Streak"
            checked={settings.notifications.streak}
            onChange={(v) => setNotifications("streak", v)}
          />
        </div>

        <div className="sCard">
          <div className="sCardTitle">Accessibility</div>

          <div className="sPrefRow">
            <div className="sPrefLabel">Text Size</div>
            <PillTabs
              value={settings.accessibility.textSize}
              onChange={(v) => setAccessibility("textSize", v)}
              columns={3}
              options={[
                { label: "Small", value: "small" },
                { label: "Medium", value: "medium" },
                { label: "Large", value: "large" },
              ]}
            />
          </div>

          <div className="sPrefRow">
            <div className="sPrefLabel">Mode</div>
            <PillTabs
              value={settings.accessibility.mode}
              onChange={(v) => setAccessibility("mode", v)}
              columns={2}
              options={[
                { label: "Light", value: "light" },
                { label: "Dark", value: "dark" },
              ]}
            />
          </div>
        </div>

        <div className="sLogoutWrap">
          <button
            type="button"
            className="sLogoutBtn"
            onClick={() => {
              logout();
              navigate("/login", { replace: true });
            }}
          >
            Log out
          </button>
        </div>
      </div>
    </div>
  );
}
