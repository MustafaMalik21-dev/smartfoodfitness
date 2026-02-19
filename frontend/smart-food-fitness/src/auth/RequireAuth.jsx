import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";

export default function RequireAuth() {
  const { auth } = useAuth();
  const loc = useLocation();

  if (!auth || !auth.token) {
    return <Navigate to="/start" replace state={{ from: loc }} />;
  }

  return <Outlet />;
}
