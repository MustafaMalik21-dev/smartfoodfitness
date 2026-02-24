import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { useAuth } from "../auth/useAuth";
import HeaderBar from "../components/HeaderBar";
import "../styles/PageShell.css";
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, CartesianGrid } from "recharts";
import "./Food.css";

function sum(arr) { // Helper function that takes an array of numbers and returns their sum, using the reduce method to accumulate the total, starting from an initial value of 0, and ensuring that non-numeric values are treated as 0 to prevent NaN results
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

  useEffect(() => { // Effect hook that runs on component mount and whenever the userId changes, responsible for fetching the user's food entry logs and goals from the API, updating the state with the fetched data to display the logged food and macro summaries, while handling loading state and cancellation to prevent state updates on unmounted components
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

  const totals = useMemo(() => { // Memoized calculation of the total protein, carbs, and fat from the logged food entries, using the sum helper function to aggregate the values for each macro across all logs, and treating missing values as 0 to ensure accurate totals for display in the macro summary chart
    return {
      protein: sum(logs.map((x) => x.proteins || 0)),
      carbs: sum(logs.map((x) => x.carbs || 0)),
      fat: sum(logs.map((x) => x.fats || 0)),
    };
  }, [logs]);

  const chartData = useMemo(() => { // Memoized preparation of the data for the macro summary bar chart, creating an array of objects representing each macro (protein, carbs, fat) with their current totals and goals, using the safeNum helper function to ensure that goal values are treated as 0 if they are missing or invalid, allowing for a consistent display of the current intake versus goals in the chart
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
