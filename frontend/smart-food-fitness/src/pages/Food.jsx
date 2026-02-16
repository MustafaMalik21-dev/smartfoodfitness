import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { useAuth } from "../auth/useAuth";
import HeaderBar from "../components/HeaderBar";
import "../styles/PageShell.css";
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, CartesianGrid } from "recharts";
import "./Food.css";

function sum(arr) {
  return arr.reduce((a, b) => a + b, 0);
}

function safeNum(v) {
  const x = Number(v);
  return Number.isFinite(x) ? x : 0;
}

export default function Food() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const userId = auth ? auth.userId : null;

  const [logs, setLogs] = useState([]);
  const [goals, setGoals] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userId) return;

    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const [logsRes, goalsRes] = await Promise.all([
          apiClient.get(`/api/food-entry-logs/user/${userId}`),
          apiClient.get(`/api/user-goals/user/${userId}`),
        ]);

        if (!cancelled) {
          setLogs(logsRes.data || []);
          setGoals(goalsRes.data || null);
        }
      } catch {
        if (!cancelled) {
          setLogs([]);
          setGoals(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [userId]);

  const totals = useMemo(() => {
    return {
      protein: sum(logs.map((x) => x.proteins || 0)),
      carbs: sum(logs.map((x) => x.carbs || 0)),
      fat: sum(logs.map((x) => x.fats || 0)),
    };
  }, [logs]);

  const chartData = useMemo(() => {
    const g = goals || {};
    return [
      { name: "Protein", Current: totals.protein, Goal: safeNum(g.proteinGoal) },
      { name: "Carbs", Current: totals.carbs, Goal: safeNum(g.carbGoal) },
      { name: "Fat", Current: totals.fat, Goal: safeNum(g.fatGoal) },
    ];
  }, [totals, goals]);

  return (
    <div className="pageShell">
      <HeaderBar title="Food" left="profile" right="notifications" />

      <div className="pageBody foodBody">
        <button className="foodPrimaryBtn" type="button" onClick={() => navigate("/food/log")}>
          Log Food
        </button>

        <div className="foodCard">
          <div className="foodCardTitle">Logged Food</div>

          <div className="foodTableHead">
            <div>Food Name</div>
            <div className="foodTableCenter">Weight</div>
            <div className="foodTableRight">Calories</div>
          </div>

          {loading ? (
            <div className="foodEmpty">Loading…</div>
          ) : logs.length === 0 ? (
            <div className="foodEmpty">No food logged yet</div>
          ) : (
            <div className="foodTableRows">
              {logs.map((x) => (
                <div className="foodRow" key={x.id}>
                  <div className="foodCell foodNameCell">{x.foodName}</div>
                  <div className="foodCell foodTableCenter">
                    {x.weightValue}
                    {x.weightUnit}
                  </div>
                  <div className="foodCell foodTableRight">{x.calories} kcal</div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="foodCard">
          <div className="foodCardTitle">Macro Summaries</div>

          <div className="macroChartCard">
            <div className="macroChartTitle">Daily Macro Intake vs Goal</div>

            <div className="macroChartFixed">
              <BarChart width={340} height={220} data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="Current" fill="#0b84ff" />
                <Bar dataKey="Goal" fill="#f59e0b" />
              </BarChart>
            </div>
          </div>
        </div>

        <button className="foodPrimaryBtn" type="button" onClick={() => navigate("/food/recipes")}>
          Find Recipes
        </button>
      </div>
    </div>
  );
}
