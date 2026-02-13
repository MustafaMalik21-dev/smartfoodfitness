const KEY = "sff_auth_v1";

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
