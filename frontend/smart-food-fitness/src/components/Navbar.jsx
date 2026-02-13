import { NavLink } from "react-router-dom";
import "./Navbar.css";

import HomeIcon from "../assets/Homeicon.png";
import DumbbellIcon from "../assets/Dumbellicon.png";
import TrackingIcon from "../assets/Trackingicon.png";
import FoodIcon from "../assets/Foodicon.png";
import SettingsIcon from "../assets/Settingsicon.png";

const NAV_ITEMS = [
  { to: "/", label: "Home", icon: HomeIcon, end: true },
  { to: "/fitness", label: "Fitness", icon: DumbbellIcon },
  { to: "/tracking", label: "Tracking", icon: TrackingIcon },
  { to: "/food", label: "Food", icon: FoodIcon },
  { to: "/settings", label: "Settings", icon: SettingsIcon },
];

export default function Navbar() {
  return (
    <nav className="bottomNav" aria-label="Bottom navigation">
      {NAV_ITEMS.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          className={({ isActive }) => `navItem ${isActive ? "active" : ""}`}
          aria-label={item.label}
        >
          <span className="navIconWrap" aria-hidden="true">
            <img className="navIcon" src={item.icon} alt="" />
          </span>
          <span className="navLabel">{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}
