import { useNavigate } from "react-router-dom";
import "./Start.css";

export default function Start() {
  const navigate = useNavigate();

  return (
    <div className="startScreen">
      <div className="startTop">
        <div className="startLogo" aria-hidden="true">
          <div className="startLogoInner">SFF</div>
        </div>

        <div className="startTitle">Smart Food &amp; Fitness</div>
        <div className="startSub">
          Track your food, workouts, and progress — all in one place.
        </div>
      </div>

      <div className="startActions">
        <button className="startBtnPrimary" type="button" onClick={() => navigate("/login")}>
          Login
        </button>
        <button className="startBtnSecondary" type="button" onClick={() => navigate("/register")}>
          Register
        </button>
      </div>

      <div className="startFooter">Brunel FYP • SmartFoodFitness</div>
    </div>
  );
}
