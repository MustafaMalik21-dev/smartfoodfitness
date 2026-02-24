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
import WorkoutHistory from "./pages/WorkoutHistory";
import RequireAuth from "./auth/RequireAuth";
import RequireOnboarding from "./auth/RequireOnboarding";
import OnboardingGuide from "./pages/auth/OnboardingGuide";
import Start from "./pages/Start";

function AppLayout() { // Layout component for the main application interface, providing a consistent structure with a navigation bar and a viewport for rendering the current page's content. The component uses the Outlet component from react-router-dom to render the matched child route within the viewport, allowing for seamless navigation between different pages of the application while maintaining the overall layout and navigation structure provided by the Navbar component.
  return (
    <div className="appLayout">
      <div className="appViewport">
        <Outlet />
      </div>
      <Navbar />
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/start" element={<Start />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<RequireAuth />}>
        <Route path="/onboarding" element={<Onboarding />} />
        <Route path="/onboarding-guide" element={<OnboardingGuide />} />

        <Route element={<RequireOnboarding />}>
          <Route element={<AppLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="fitness" element={<Fitness />} />
            <Route path="tracking" element={<Tracking />} />
            <Route path="food" element={<Food />} />
            <Route path="food/log" element={<LogFood />} />
            <Route path="food/recipes" element={<FindRecipes />} />
            <Route path="workout-history" element={<WorkoutHistory />} />
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

      <Route path="*" element={<Navigate to="/start" replace />} />
    </Routes>
  );
}
