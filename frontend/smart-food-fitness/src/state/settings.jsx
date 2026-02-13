import { createContext, useContext, useEffect, useMemo, useState } from "react";

const SETTINGS_STORAGE_KEY = "sff_settings_v1"; //Local storage key for settings

const DEFAULTS = { //Default settings
  units: { weight: "kg", height: "ft" },  
  tracking: { macros: true, weight: true, workouts: true }, 
  notifications: { workouts: true, food: true, streak: true }, 
  accessibility: { textSize: "small", mode: "light" },
};

function safeParse(json) { 
  try {
    return JSON.parse(json); 
  } catch {
    return null;
  }
}

function mergeDefaults(saved) { //Merge saved settings with defaults
  if (!saved || typeof saved !== "object") return DEFAULTS; //Return defaults if no saved settings

  return {
    units: { ...DEFAULTS.units, ...(saved.units || {}) }, //Merge each category
    tracking: { ...DEFAULTS.tracking, ...(saved.tracking || {}) },
    notifications: { ...DEFAULTS.notifications, ...(saved.notifications || {}) },
    accessibility: { ...DEFAULTS.accessibility, ...(saved.accessibility || {}) },
  };
}

function loadSettings() { //Load settings from local storage
  const raw = localStorage.getItem(SETTINGS_STORAGE_KEY);
  const parsed = safeParse(raw);
  return mergeDefaults(parsed);
}

function saveSettings(next) { //Save settings to local storage
  localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify(next));
}

const SettingsContext = createContext(null); //Create context for settings

export function SettingsProvider({ children }) { 
  const [settings, setSettings] = useState(loadSettings); //Initialize settings state

  useEffect(() => {
    saveSettings(settings); //Save settings on change
  }, [settings]);

  useEffect(() => {
    const root = document.documentElement; //Update document root attributes for accessibility

    const mode = settings.accessibility.mode; //light or dark
    const textSize = settings.accessibility.textSize;

    root.dataset.mode = mode;
    root.dataset.textsize = textSize;

    const scale = textSize === "small" ? 0.95 : textSize === "large" ? 1.08 : 1.0; //Calculate scale
    root.style.setProperty("--app-scale", String(scale)); //Apply scale to CSS variable
  }, [settings]);

  const api = useMemo(() => {
    const update = (updater) => {
      setSettings((prev) => (typeof updater === "function" ? updater(prev) : updater)); //Update settings state
    };

    const setUnit = (key, value) =>
      update((p) => ({ ...p, units: { ...p.units, [key]: value } })); //Update unit settings

    const setTracking = (key, value) =>
      update((p) => ({ ...p, tracking: { ...p.tracking, [key]: value } }));

    const setNotifications = (key, value) =>
      update((p) => ({ ...p, notifications: { ...p.notifications, [key]: value } }));

    const setAccessibility = (key, value) =>
      update((p) => ({ ...p, accessibility: { ...p.accessibility, [key]: value } }));

    return {
      settings,
      update,
      setUnit,
      setTracking,
      setNotifications,
      setAccessibility,
    };
  }, [settings]);

  return <SettingsContext.Provider value={api}>{children}</SettingsContext.Provider>; 
}

export function useSettings() { //Custom hook to access settings context
  const ctx = useContext(SettingsContext); 
  if (!ctx) throw new Error("useSettings must be used inside SettingsProvider"); 
  return ctx;
}
