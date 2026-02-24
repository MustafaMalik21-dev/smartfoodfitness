import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";
// This component is a higher-order component that checks if the user has completed the onboarding process by accessing the authentication state from the AuthContext using the useAuth hook, and if the user is not authenticated, it redirects them to the "/login" route, while if the user is authenticated but has not completed onboarding (i.e., their onboardingComplete property is not true), it redirects them to the "/onboarding" route, allowing access to protected routes within the application that require both authentication and completion of the onboarding process for users to access certain features or sections of the application after they have logged in but before they have completed their profile setup
export default function RequireOnboarding() {
  const { auth, isAuthenticated } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!auth || auth.onboardingComplete !== true) return <Navigate to="/onboarding" replace />;

  return <Outlet />;
}
