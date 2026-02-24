import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import "./Auth.css";
// This component renders the registration page of the application, allowing users to create a new account by entering their display name, email, and password, and upon successful registration, it redirects them to the onboarding page to complete their profile setup, while also handling error states and providing a link to the login page for users who already have an account. The component includes validation for the input fields to ensure that the user provides valid information before attempting to register.
function isValidEmail(email) {
  const e = String(email || "").trim();
  if (!e) return false;
  if (!e.includes("@")) return false;
  const parts = e.split("@");
  if (parts.length !== 2) return false;
  if (!parts[0] || !parts[1]) return false;
  if (!parts[1].includes(".")) return false;
  if (parts[1].startsWith(".") || parts[1].endsWith(".")) return false;
  return true;
}

export default function Register() { // This component renders the registration page of the application, allowing users to create a new account by entering their display name, email, and password, and upon successful registration, it redirects them to the onboarding page to complete their profile setup, while also handling error states and providing a link to the login page for users who already have an account. The component includes validation for the input fields to ensure that the user provides valid information before attempting to register.
  const nav = useNavigate();
  const { register } = useAuth();

  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  async function onSubmit(e) {
    e.preventDefault();
    if (busy) return;

    try {
      setBusy(true);
      setErr("");

      const dn = displayName.trim();
      const em = email.trim();

      if (!dn) {
        setErr("Please enter a display name.");
        return;
      }

      if (!em) {
        setErr("Please enter an email.");
        return;
      }

      if (!isValidEmail(em)) {
        setErr("Please enter a valid email (e.g. you@example.com).");
        return;
      }

      if (!password) {
        setErr("Please enter a password.");
        return;
      }

      if (password.length < 8) {
        setErr("Password must be at least 8 characters long.");
        return;
      }

      await register(dn, em, password);
      nav("/onboarding", { replace: true });
    } catch (ex) {
      const data = ex?.response?.data;

      if (data?.fieldErrors && typeof data.fieldErrors === "object") {
        const fe = data.fieldErrors;
        if (fe.password) {
          setErr(String(fe.password));
        } else if (fe.email) {
          setErr(String(fe.email));
        } else if (fe.displayName) {
          setErr(String(fe.displayName));
        } else {
          const firstKey = Object.keys(fe)[0];
          setErr(firstKey ? String(fe[firstKey]) : "Registration failed.");
        }
      } else if (data?.message) {
        setErr(String(data.message));
      } else {
        setErr("Registration failed.");
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="authScreen">
      <div className="authTopBar">
        <div className="authAppTitle">Smart Food &amp; Fitness</div>
      </div>

      <div className="authBody">
        <div className="authPanel">
          <h1 className="authH1">Create account</h1>
          <p className="authSub">Let’s set you up. You’ll do a quick onboarding next.</p>

          {err ? <div className="authErr">{err}</div> : null}

          <form onSubmit={onSubmit} className="authForm">
            <div className="authField">
              <label className="authLabel">Display name</label>
              <input
                className="authInput"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                autoComplete="nickname"
                placeholder="e.g. Bob"
              />
            </div>

            <div className="authField">
              <label className="authLabel">Email</label>
              <input
                className="authInput"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                inputMode="email"
                placeholder="you@example.com"
              />
            </div>

            <div className="authField">
              <label className="authLabel">Password</label>
              <input
                className="authInput"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
                placeholder="enter password"
              />
            </div>

            <button className="authBtn" type="submit" disabled={busy}>
              {busy ? "Creating…" : "Create account"}
            </button>
          </form>

          <div className="authFooter">
            Already have an account?{" "}
            <Link className="authLink" to="/login">
              Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
