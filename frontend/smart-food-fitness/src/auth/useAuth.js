import { useContext } from "react";
import { AuthContext } from "./AuthContext";
// This custom hook provides a convenient way for components to access the authentication state and functions from the AuthContext, allowing them to easily check if a user is authenticated, access the user's authentication information, and perform authentication-related actions such as logging in, registering, or logging out by using the context values provided by the AuthProvider component when they need to interact with the authentication system of the application
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");

  return {
    ...ctx,
    loggedIn: ctx.isAuthenticated,
    user: ctx.auth,
  };
}
