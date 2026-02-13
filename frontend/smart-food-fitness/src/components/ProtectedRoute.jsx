import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

export default function ProtectedRoute({ requireOnboarding = false }) {
  const { isAuthenticated, auth } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (requireOnboarding && (!auth || auth.onboardingComplete !== true)) {
    return <Navigate to="/onboarding" replace />;
  }

  return <Outlet />;
}
