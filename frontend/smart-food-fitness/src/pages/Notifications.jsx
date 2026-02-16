import { useEffect, useMemo, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import apiClient from "../api/apiClient";
import "../styles/PageShell.css";
import PFP from "../assets/PFP.png";
import NotifBell from "../assets/NotifBell.png";
import Dumbellicon from "../assets/Dumbellicon.png";
import Streakicon from "../assets/Streakicon.png";
import Settingsicon from "../assets/Settingsicon.png";
import Foodicon from "../assets/Foodicon.png";
import { getUserId } from "../auth/authStorage";

import "./Notifications.css";

function toDateLabel(iso) { // convert ISO date string to local date-time label
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return ""; 
  return d.toLocaleString();
}

function normalizeType(notificationType) { // normalize notification type string
  const raw = String(notificationType || "").trim().toLowerCase();
  if (!raw) return "general";

  if (raw === "food") return "food";
  if (raw === "workout") return "workout";
  if (raw === "streak") return "streak";
  if (raw === "system") return "system";
  if (raw === "reminder") return "reminder";
  if (raw === "general") return "general";

  return raw;
}

function iconForType(type) { // get icon image source for notification type
  if (type === "food") return Foodicon;
  if (type === "workout") return Dumbellicon;
  if (type === "streak") return Streakicon;
  if (type === "system") return Settingsicon;
  return NotifBell; // general + reminder
}

function isRead(n) { // check if notification is read
  return Boolean(n?.isRead || n?.readAt);
}


export default function Notifications() { // Notifications page
  const navigate = useNavigate();
  const location = useLocation();

  const userId = getUserId();
  const cameFrom = useMemo(() => location.state?.from || null, [location.state]);

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const [filter, setFilter] = useState("all"); // all, food, workout, streak, unread

  async function load() {
    try {
      setLoading(true); // show loading state
      setErr("");

      const res = await apiClient.get("/api/notifications", {
        params: { userId }, // fetch notifications for current user
      });

      setItems(Array.isArray(res.data) ? res.data : []); // set notifications
    } catch {
      setErr("Failed to load notifications.");
      setItems([]); // clear items on error
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!userId) return;
    load();
  }, [userId]);

  async function markRead(notificationId) { // mark a notification as read
    if (!notificationId) return;

    setItems((prev) => // optimistically mark as read
      prev.map((n) =>
        n.id === notificationId ? { ...n, isRead: true, readAt: n.readAt || new Date().toISOString() } : n // mark read with current time if not set
      )
    );

    try {
      const res = await apiClient.put(`/api/notifications/${notificationId}/read`, { isRead: true }); // send mark read request
      const updated = res.data; // update notification from response
      if (updated?.id) {
        setItems((prev) => prev.map((n) => (n.id === updated.id ? { ...n, ...updated } : n))); // update item with server data
      }
    } catch {
      await load();
    }
  }

  const filteredItems = useMemo(() => { // filter notifications based on selected filter
    const list = Array.isArray(items) ? items : [];

    if (filter === "all") return list; // no filter
    if (filter === "unread") return list.filter((n) => !isRead(n)); // unread only

    return list.filter((n) => normalizeType(n?.notificationType) === filter); // filter by type
  }, [items, filter]);

  return (
    <div className="pageShell notifShell">
      <div className="notifTop">
        <button
          type="button"
          className="notifBackBtn"
          onClick={() => {
            if (cameFrom) navigate(cameFrom);
            else navigate(-1);
          }}
        >
          Back
        </button>

        <div className="notifTitle">Notifications</div>

        <button
          type="button"
          className="notifIconBtn"
          aria-label="Profile"
          onClick={() => navigate("/profile", { state: { from: location.pathname } })}
        >
          <span className="notifIconCircle">
            <img className="notifIconImg" src={PFP} alt="" aria-hidden="true" />
          </span>
        </button>
      </div>

      <div className="pageBody notifBody">
        <div className="notifFilters" role="tablist" aria-label="Notification filters">
          <button
            type="button"
            className={filter === "all" ? "notifFilter notifFilterOn" : "notifFilter"}
            onClick={() => setFilter("all")}
          >
            All
          </button>
          <button
            type="button"
            className={filter === "food" ? "notifFilter notifFilterOn" : "notifFilter"}
            onClick={() => setFilter("food")}
          >
            Food
          </button>
          <button
            type="button"
            className={filter === "workout" ? "notifFilter notifFilterOn" : "notifFilter"}
            onClick={() => setFilter("workout")}
          >
            Workouts
          </button>
          <button
            type="button"
            className={filter === "streak" ? "notifFilter notifFilterOn" : "notifFilter"}
            onClick={() => setFilter("streak")}
          >
            Streak
          </button>
          <button
            type="button"
            className={filter === "unread" ? "notifFilter notifFilterOn" : "notifFilter"}
            onClick={() => setFilter("unread")}
          >
            Unread
          </button>
        </div>

        {loading && <div className="notifHint">Loading…</div>}
        {!loading && err && <div className="notifHint">{err}</div>}
        {!loading && !err && filteredItems.length === 0 && <div className="notifHint">No notifications.</div>}

        {!loading && !err && filteredItems.length > 0 && ( // display notification list
          <div className="notifList">
            {filteredItems.map((n) => {
              const read = isRead(n);
              const t = normalizeType(n?.notificationType);
              const iconSrc = iconForType(t);

              return (
                <button
                  key={n.id}
                  type="button"
                  className={read ? "notifCard" : "notifCard notifCardUnread"}
                  onClick={() => {
                    if (!read) markRead(n.id);
                  }}
                >
                  <div className="notifLeftIcon" aria-hidden="true">
                    <img className="notifLeftIconImg" src={iconSrc} alt="" aria-hidden="true" />
                  </div>

                  <div className="notifMain">
                    <div className="notifCardTop">
                      <div className="notifCardTitleRow">
                        {!read && <span className="notifDot" aria-hidden="true" />}
                        <div className="notifCardTitle">{n?.title || "Notification"}</div>
                      </div>
                      <div className="notifCardTime">{toDateLabel(n?.createdAt)}</div>
                    </div>

                    <div className="notifCardMsg">{n?.message || ""}</div>

                    <div className="notifCardFooter">
                      <span className={read ? "badge badgeRead" : "badge badgeUnread"}>
                        {read ? "Read" : "Unread"}
                      </span>
                      {!read && <span className="notifTap">Tap to mark read</span>}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
