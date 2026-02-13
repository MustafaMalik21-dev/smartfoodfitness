import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import PFP from "../assets/PFP.png";
import NotifBell from "../assets/NotifBell.png";
import "./HeaderBar.css";

function capCount(n) {
  const x = Number(n);
  if (!Number.isFinite(x) || x <= 0) return 0;
  return x;
}

export default function HeaderBar({
  title,
  left = "profile",
  right = "notifications",
  backTo = "/fitness",
}) {
  const navigate = useNavigate();
  const userId = useMemo(() => getUserId(), []);

  const [unreadCount, setUnreadCount] = useState(0);

  const onLeftClick = () => {
    if (left === "back") {
      navigate(backTo);
      return;
    }
    if (left === "profile") navigate("/profile");
  };

  const onRightClick = () => {
    if (right === "notifications") {
      navigate("/notifications");
      return;
    }
    if (right === "search") return;
  };

  useEffect(() => {
    if (right !== "notifications") return;
    if (!userId) {
      setUnreadCount(0);
      return;
    }

    let cancelled = false;

    async function loadUnread() {
      try {
        const res = await apiClient.get("/api/notifications/unread-count", {
          params: { userId },
        });
        if (!cancelled) setUnreadCount(capCount(res.data));
      } catch {
        if (!cancelled) setUnreadCount(0);
      }
    }

    loadUnread();

    const poll = setInterval(loadUnread, 15000);

    const onFocus = () => loadUnread();
    const onVisibility = () => {
      if (document.visibilityState === "visible") loadUnread();
    };

    window.addEventListener("focus", onFocus);
    document.addEventListener("visibilitychange", onVisibility);

    return () => {
      cancelled = true;
      clearInterval(poll);
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [right, userId]);

  const badgeText = unreadCount > 9 ? "9+" : String(unreadCount);

  return (
    <div className="headerBar">
      <div className="headerSide">
        {left === "none" ? null : left === "back" ? (
          <button className="backBtn" type="button" onClick={onLeftClick}>
            Back
          </button>
        ) : (
          <button className="iconBtn" type="button" aria-label="Profile" onClick={onLeftClick}>
            <span className="iconCircle">
              <img className="iconImg" src={PFP} alt="" aria-hidden="true" />
            </span>
          </button>
        )}
      </div>

      <div className="pageTitle">{title}</div>

      <div className="headerSide headerRight">
        {right === "none" ? null : right === "search" ? (
          <button className="iconBtn" type="button" aria-label="Search" onClick={onRightClick}>
            <span className="iconCircle">🔍</span>
          </button>
        ) : (
          <button className="iconBtn iconBtnBadge" type="button" aria-label="Notifications" onClick={onRightClick}>
            <span className="iconCircle">
              <img className="iconImg" src={NotifBell} alt="" aria-hidden="true" />
            </span>

            {unreadCount > 0 ? <span className="notifBadge">{badgeText}</span> : null}
          </button>
        )}
      </div>
    </div>
  );
}
