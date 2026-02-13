import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";

export default function RequireOnboarding() {
  const { auth, isAuthenticated } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!auth || auth.onboardingComplete !== true) return <Navigate to="/onboarding" replace />;

  return <Outlet />;
}
