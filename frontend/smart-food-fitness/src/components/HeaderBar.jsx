import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { getUserId } from "../auth/authStorage";
import PFP from "../assets/PFP.png";
import NotifBell from "../assets/NotifBell.png";
import "./HeaderBar.css";

function capCount(n) { // Helper function to cap the unread notifications count at a maximum of 9, accepting a number as input, converting it to a finite number, and returning 0 if the input is not a valid positive number, otherwise returning the number itself if it is 9 or less, or "9+" if it exceeds 9, allowing the application to display a user-friendly badge on the notifications icon that indicates the number of unread notifications without overwhelming the user with large numbers
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

    async function loadUnread() { // Function to load the count of unread notifications for the user, making a GET request to the backend API endpoint for retrieving the unread notifications count based on the user's ID, and updating the state with the retrieved count while handling any potential errors by setting the count to 0 if the request fails, allowing the application to display an accurate badge on the notifications icon that reflects the number of unread notifications for the user
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

  return ( // The HeaderBar component renders a header bar with a title and optional left and right buttons, where the left button can be either a profile icon or a back button that navigates to a specified route when clicked, and the right button can be either a notifications icon that shows a badge with the count of unread notifications for the user or a search icon, allowing for consistent navigation and access to important features like the user's profile and notifications across different pages of the application while providing visual feedback on the number of unread notifications through the badge
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
