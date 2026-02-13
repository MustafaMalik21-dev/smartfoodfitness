import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import "./Auth.css";

export default function Register() {
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

      if (!displayName.trim()) {
        setErr("Please enter a display name.");
        return;
      }
      if (!email.trim()) {
        setErr("Please enter an email.");
        return;
      }
      if (!password) {
        setErr("Please enter a password.");
        return;
      }

      await register(displayName.trim(), email.trim(), password);
      nav("/onboarding", { replace: true });
    } catch (ex) {
      setErr((ex && ex.response && ex.response.data && ex.response.data.message) || "Registration failed.");
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
                placeholder="e.g. Mustafa"
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
                placeholder="••••••••"
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
