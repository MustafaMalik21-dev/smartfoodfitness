import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
// This component is a higher-order component that checks if the user is authenticated and optionally if they have completed the onboarding process by accessing the authentication state from the AuthContext using the useAuth hook, and if the user is not authenticated, it redirects them to the "/login" route, passing the current location in the state so that they can be redirected back to their original destination after successful authentication, while if the user is authenticated but has not completed onboarding (when requireOnboarding is true), it redirects them to the "/onboarding" route, allowing access to protected routes within the application that require authentication and optionally completion of the onboarding process for users to access certain features or sections of the application after they have logged in but before they have completed their profile setup
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
