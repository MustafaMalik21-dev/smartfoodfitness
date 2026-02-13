import { Routes, Route, Navigate, Outlet } from "react-router-dom";
import Navbar from "./components/Navbar";

import Dashboard from "./pages/Dashboard";
import Fitness from "./pages/Fitness";
import Tracking from "./pages/Tracking";
import Food from "./pages/Food";
import Settings from "./pages/Settings";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";
import ExerciseEncyclopedia from "./pages/ExerciseEncyclopedia";
import Workout from "./pages/Workout";
import WorkoutPlans from "./pages/WorkoutPlans";
import LogFood from "./pages/LogFood";
import FindRecipes from "./pages/FindRecipes";
import WorkoutPlanDetail from "./pages/WorkoutPlanDetail";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Onboarding from "./pages/auth/Onboarding";

import RequireAuth from "./auth/RequireAuth";
import RequireOnboarding from "./auth/RequireOnboarding";

function AppLayout() {
  return (
    <div style={{ height: "100%", position: "relative" }}>
      <div style={{ height: "100%", overflowY: "auto", paddingBottom: "84px" }}>
        <Outlet />
      </div>
      <Navbar />
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<RequireAuth />}>
        <Route path="/onboarding" element={<Onboarding />} />

        <Route element={<RequireOnboarding />}>
          <Route element={<AppLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="fitness" element={<Fitness />} />
            <Route path="tracking" element={<Tracking />} />
            <Route path="food" element={<Food />} />
            <Route path="food/log" element={<LogFood />} />
            <Route path="food/recipes" element={<FindRecipes />} />
            <Route path="settings" element={<Settings />} />
            <Route path="notifications" element={<Notifications />} />
            <Route path="profile" element={<Profile />} />
            <Route path="exercise-encyclopedia" element={<ExerciseEncyclopedia />} />
            <Route path="workout" element={<Workout />} />
            <Route path="workout-plans" element={<WorkoutPlans />} />
            <Route path="/workout-plans/:id" element={<WorkoutPlanDetail />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
