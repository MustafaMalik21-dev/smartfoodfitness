import { useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import "./Auth.css";

export default function Login() {
  const nav = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = useMemo(() => {
    const f = location.state && location.state.from ? location.state.from : "/";
    return f;
  }, [location.state]);

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

      const next = await login(email, password);

      if (next.onboardingComplete === true) {
        nav(from, { replace: true });
        return;
      }

      nav("/onboarding", { replace: true });
    } catch (ex) {
      setErr((ex && ex.response && ex.response.data && ex.response.data.message) || "Login failed.");
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
          <h1 className="authH1">Welcome back</h1>
          <p className="authSub">Sign in to continue tracking your progress.</p>

          {err ? <div className="authErr">{err}</div> : null}

          <form onSubmit={onSubmit} className="authForm">
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
                autoComplete="current-password"
                placeholder="enter password"
              />
            </div>

            <button className="authBtn" type="submit" disabled={busy}>
              {busy ? "Signing in…" : "Sign in"}
            </button>
          </form>

          <div className="authFooter">
            No account?{" "}
            <Link className="authLink" to="/register">
              Create one
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
