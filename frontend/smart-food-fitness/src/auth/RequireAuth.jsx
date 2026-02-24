import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";
// This component is a higher-order component that checks if the user is authenticated by accessing the authentication state from the AuthContext using the useAuth hook, and if the user is not authenticated (i.e., there is no valid authentication token), it redirects them to the "/start" route, passing the current location in the state so that they can be redirected back to their original destination after successful authentication, while if the user is authenticated, it renders the child components defined in the Outlet, allowing access to protected routes within the application that require authentication
export default function RequireAuth() {
  const { auth } = useAuth();
  const loc = useLocation();

  if (!auth || !auth.token) {
    return <Navigate to="/start" replace state={{ from: loc }} />;
  }

  return <Outlet />;
}
