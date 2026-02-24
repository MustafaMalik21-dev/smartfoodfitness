const KEY = "sff_auth_v1";
// This module provides functions to manage authentication state in the application, including storing and retrieving authentication information from session storage, as well as helper functions to get the authentication token and user ID, and to check if a user is authenticated, allowing the application to persist the user's authentication state across page reloads and provide easy access to authentication-related information when needed for making authenticated API requests or checking if a user is logged in
function store() {
  return window.sessionStorage;
}

export function getAuth() {
  try {
    const raw = store().getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setAuth(auth) {
  store().setItem(KEY, JSON.stringify(auth));
}

export function clearAuth() {
  try {
    store().removeItem(KEY);
  } catch {}
}

export function getToken() {
  const a = getAuth();
  return a && a.token ? a.token : "";
}

export function getUserId() {
  const a = getAuth();
  return a && typeof a.userId !== "undefined" ? a.userId : null;
}

export function isAuthed() {
  return !!getToken();
}
