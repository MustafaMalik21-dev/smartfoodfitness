import { createContext, useEffect, useMemo, useState } from "react";
import apiClient from "../api/apiClient";
import { clearAuth, getAuth, setAuth as persistAuth } from "./authStorage";
// Create an authentication context using React's createContext, and define an AuthProvider component that manages the authentication state of the application, including functions for logging in, registering, and logging out users, as well as persisting the authentication state across page reloads and clearing it when the user leaves the page, allowing components within the application to access the authentication state and functions through the context when they need to perform authentication-related actions or check if a user is authenticated
export const AuthContext = createContext(null);

function coerceBool(v) {
  if (v === true) return true;
  if (v === false) return false;

  if (typeof v === "string") {
    const s = v.trim().toLowerCase();
    if (s === "true") return true;
    if (s === "false") return false;
  }

  if (typeof v === "number") {
    if (v === 1) return true;
    if (v === 0) return false;
  }

  return false;
}
 
export function AuthProvider({ children }) { // AuthProvider component that manages the authentication state of the application, including functions for logging in, registering, and logging out users, as well as persisting the authentication state across page reloads and clearing it when the user leaves the page, allowing components within the application to access the authentication state and functions through the context when they need to perform authentication-related actions or check if a user is authenticated
  const [auth, setAuthState] = useState(() => getAuth());

  useEffect(() => {
    const clear = () => {
      clearAuth();
      setAuthState(null);
    };

    window.addEventListener("pagehide", clear);
    window.addEventListener("beforeunload", clear);

    return () => {
      window.removeEventListener("pagehide", clear);
      window.removeEventListener("beforeunload", clear);
    };
  }, []);

  useEffect(() => {
    if (auth) persistAuth(auth);
    else clearAuth();
  }, [auth]);

  const value = useMemo(() => {
    async function fetchProfileOnboardingComplete(userId, token) { // Helper function to fetch the onboarding completion status of a user's profile, accepting the user ID and authentication token as parameters, making a GET request to the backend API endpoint for retrieving the user profile data, including the onboarding completion status, and returning a boolean value indicating whether the onboarding process is complete based on the response data when users log in or register and their onboarding completion status needs to be checked to determine if they should be directed to complete their profile setup
      const res = await apiClient.get(`/api/user-profile/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return coerceBool(res.data && res.data.onboardingComplete);
    }

    async function login(email, password) {
      const res = await apiClient.post("/api/auth/login", { email, password });

      const token = res.data.token;

      const next = {
        userId: res.data.userId,
        email: res.data.email,
        displayName: res.data.displayName,
        token: token,
        onboardingComplete: coerceBool(res.data.onboardingComplete),
      };

      setAuthState(next);

      if (next.userId && token && next.onboardingComplete !== true) { // If the user has a valid ID and token but their onboarding is not complete, attempt to fetch the onboarding completion status from the backend API to ensure that the authentication state is updated with the correct onboarding status, allowing the application to direct the user to complete their profile setup if necessary when they log in
        try {
          const oc = await fetchProfileOnboardingComplete(next.userId, token);
          if (oc === true) {
            const updated = {
              userId: next.userId,
              email: next.email,
              displayName: next.displayName,
              token: next.token,
              onboardingComplete: true,
            };
            setAuthState(updated);
            return updated;
          }
        } catch {
        }
      }

      return next;
    }

    async function register(displayName, email, password) { // Function to register a new user, accepting the display name, email, and password as parameters, making a POST request to the backend API endpoint for user registration with the provided information, receiving the response containing the user's authentication token and profile information, creating an authentication state object with the relevant data including the onboarding completion status, updating the authentication state with this new object, and returning it to be used in the application after successful registration when users sign up for a new account and need to be authenticated immediately after registration
      const res = await apiClient.post("/api/auth/register", {
        displayName,
        email,
        password,
      });

      const next = {
        userId: res.data.userId,
        email: res.data.email,
        displayName: res.data.displayName,
        token: res.data.token,
        onboardingComplete: coerceBool(res.data.onboardingComplete),
      };

      setAuthState(next);
      return next;
    }

    function logout() {
      setAuthState(null);
      clearAuth();
    }

    function setAuth(next) {
      setAuthState(next);
    }

    return {
      auth,
      isAuthenticated: !!(auth && auth.token),
      login,
      register,
      logout,
      setAuth,
    };
  }, [auth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
