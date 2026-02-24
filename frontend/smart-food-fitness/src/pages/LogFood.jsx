import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { useAuth } from "../auth/useAuth";
import "./LogFood.css";

function n(v) { // Helper function that takes a value and attempts to convert it to a number, returning the number if it's finite, or 0 if it's not a valid number, used to safely handle numeric inputs for calculations without risking NaN results
  const x = Number(v);
  return Number.isFinite(x) ? x : 0;
}

function roundInt(v) {
  const x = Number(v);
  if (!Number.isFinite(x)) return 0;
  return Math.round(x);
}

function calcFromPer100(per100, grams) { // Helper function that calculates the total amount of a macro (calories, protein, carbs, fat) based on its per 100g value and the actual weight in grams, using the formula (per100 * grams) / 100 to scale the per 100g value to the specified weight, and ensuring that non-numeric inputs are treated as 0 to prevent NaN results
  return (n(per100) * n(grams)) / 100;
}

function clampNonNeg(v) {
  const x = Number(v);
  if (!Number.isFinite(x)) return 0;
  return Math.max(0, x);
}

export default function LogFood() { // Main component for the Log Food page, responsible for allowing users to search for foods, view recent foods, and log new food entries, utilizing state and effect hooks to manage data fetching, user interactions, and modal dialogs for adding food entries, while ensuring a responsive and user-friendly interface for tracking food intake
  const navigate = useNavigate();
  const { auth } = useAuth();
  const userId = auth ? auth.userId : null;

  const [tab, setTab] = useState("Popular");
  const [q, setQ] = useState("");

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const [items, setItems] = useState([]);

  const [recentLoading, setRecentLoading] = useState(false);
  const [recentErr, setRecentErr] = useState("");
  const [recentItems, setRecentItems] = useState([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [grams, setGrams] = useState("100");
  const [saving, setSaving] = useState(false);

  const [manualOpen, setManualOpen] = useState(false);
  const [mName, setMName] = useState("");
  const [mGrams, setMGrams] = useState("100");
  const [mKcal, setMKcal] = useState("");
  const [mP, setMP] = useState("");
  const [mC, setMC] = useState("");
  const [mF, setMF] = useState("");
  const [manualSaving, setManualSaving] = useState(false);
  const [manualErr, setManualErr] = useState("");

  const query = useMemo(() => q.trim(), [q]);

  useEffect(() => { // Effect hook that runs whenever the modalOpen or manualOpen state changes, responsible for preventing background scrolling when either the add food modal or manual entry modal is open by setting the body's overflow style to "hidden", and restoring it when the modals are closed, ensuring a better user experience by keeping the focus on the modal content without unintended scrolling of the background page
    if (!modalOpen && !manualOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [modalOpen, manualOpen]);

  useEffect(() => {
    let cancelled = false;

    async function runPopular() {
      setErr("");
      setLoading(true);
      try {
        const res = await apiClient.get("/api/food-database/search", {
          params: { q: query || "chicken", limit: 25 },
        });

        const next = res && res.data && res.data.items ? res.data.items : [];
        if (!cancelled) setItems(next);
      } catch {
        if (!cancelled) setErr("Failed to load foods.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    if (tab === "Popular") runPopular();
    else setItems([]);

    return () => {
      cancelled = true;
    };
  }, [query, tab]);

  useEffect(() => { // Effect hook that runs whenever the selected tab or userId changes, responsible for loading the recent food entries for the user when the "Recent" tab is active, fetching the data from the API and mapping it to a format suitable for display in the recent foods list, while handling loading state, errors, and cancellation to prevent state updates on unmounted components, ensuring that users can see their recently logged foods when they switch to the "Recent" tab
    let cancelled = false;

    async function loadRecent() {
      if (tab !== "Recent") return;

      if (!userId) {
        setRecentItems([]);
        setRecentErr("Log in to view your recent foods.");
        return;
      }

      setRecentErr("");
      setRecentLoading(true);

      try { // Fetch the recent food entry logs for the user from the API, mapping the response data to a list of recent food items with their name, weight, calories, and derived per 100g values for display in the recent foods list, while ensuring that non-numeric values are treated as 0 to prevent NaN results and providing a fallback for missing data to maintain a consistent user experience
        const res = await apiClient.get(`/api/food-entry-logs/user/${userId}`);
        const list = Array.isArray(res.data) ? res.data : Array.isArray(res.data?.items) ? res.data.items : [];

        if (cancelled) return;

        const mapped = list.map((x, i) => {
          const weight = clampNonNeg(x?.weightValue);
          const kcal = clampNonNeg(x?.calories);
          const p = clampNonNeg(x?.proteins);
          const c = clampNonNeg(x?.carbs);
          const f = clampNonNeg(x?.fats);

          const per100 = weight > 0 ? (kcal / weight) * 100 : 0;

          return {
            key: String(x?.id ?? `${x?.foodName ?? "food"}-${i}`),
            name: String(x?.foodName || "Food item"),
            grams: roundInt(weight),
            kcal: roundInt(kcal),
            p: roundInt(p),
            c: roundInt(c),
            f: roundInt(f),
            per100Derived: {
              kcal: per100,
              p: weight > 0 ? (p / weight) * 100 : 0,
              c: weight > 0 ? (c / weight) * 100 : 0,
              f: weight > 0 ? (f / weight) * 100 : 0,
            },
          };
        });

        setRecentItems(mapped);
      } catch {
        if (!cancelled) {
          setRecentItems([]);
          setRecentErr("Could not load recent foods.");
        }
      } finally {
        if (!cancelled) setRecentLoading(false);
      }
    }

    loadRecent();

    return () => {
      cancelled = true;
    };
  }, [tab, userId]);

  const rows = useMemo(() => { // Memoized preparation of the list of food items to display in the "Popular" tab, mapping the fetched items from the API to a format suitable for display in the food search results, including the food name, brand, per 100g macros, and image URL, while ensuring that non-numeric values are treated as 0 to prevent NaN results and providing fallbacks for missing data to maintain a consistent user experience when browsing popular foods
    if (tab !== "Popular") return [];

    return items.map((x) => {
      const display = x.brand ? `${x.name} (${x.brand})` : x.name;

      return {
        fdcId: x.fdcId,
        name: display || "Food item",
        per100: {
          kcal: x.kcalPer100g,
          p: x.proteinPer100g,
          c: x.carbsPer100g,
          f: x.fatPer100g,
        },
        imageUrl: x.imageUrl || null,
      };
    });
  }, [items, tab]);

  const gramsNum = useMemo(() => { // Memoized calculation of the numeric value of the grams input for the add food modal, using the clampNonNeg helper function to ensure that the value is treated as 0 if it's not a valid number or if it's negative, providing a safe and consistent way to handle user input for the weight of the food being added without risking NaN results or negative values that don't make sense in this context
    const g = Number(String(grams).trim());
    if (!Number.isFinite(g)) return 0;
    if (g < 1) return 0;
    return g;
  }, [grams]);

  const modalTotals = useMemo(() => { // Memoized calculation of the total calories, protein, carbs, and fat for the food entry being added in the modal, based on the selected food's per 100g values and the entered weight in grams, using the calcFromPer100 helper function to scale the per 100g values to the specified weight, and rounding the results to integers for display in the modal, while ensuring that non-numeric inputs are treated as 0 to prevent NaN results and providing a fallback of 0 for all macros if no food is selected, allowing users to see the calculated macros for the food they are adding before confirming
    if (!selected) return { kcal: 0, p: 0, c: 0, f: 0 }; 

    const kcal = calcFromPer100(selected.per100.kcal, gramsNum);
    const p = calcFromPer100(selected.per100.p, gramsNum);
    const c = calcFromPer100(selected.per100.c, gramsNum);
    const f = calcFromPer100(selected.per100.f, gramsNum);

    return {
      kcal: roundInt(kcal),
      p: roundInt(p),
      c: roundInt(c),
      f: roundInt(f),
    };
  }, [selected, gramsNum]);

  function openAdd(row) {
    setSelected(row);
    setGrams("100");
    setModalOpen(true);
    setErr("");
  }

  function openAddFromRecent(r) {
    const row = {
      fdcId: null,
      name: r.name,
      per100: {
        kcal: r.per100Derived.kcal,
        p: r.per100Derived.p,
        c: r.per100Derived.c,
        f: r.per100Derived.f,
      },
      imageUrl: null,
    };
    setSelected(row);
    setGrams(String(r.grams || 100));
    setModalOpen(true);
    setErr("");
  }

  function closeModal() { // Function to close the add food modal, resetting the selected food and grams input, and ensuring that if a save operation is in progress, the modal cannot be closed to prevent interrupting the save process, providing a smoother user experience when adding food entries
    if (saving) return;
    setModalOpen(false);
    setSelected(null);
  }

  async function confirmAdd() {
    if (!userId) {
      navigate("/login");
      return;
    }
    if (!selected) return;
    if (gramsNum <= 0) {
      setErr("Please enter a valid weight.");
      return;
    }

    setSaving(true);
    setErr("");

    try {
      await apiClient.post("/api/food-entry-logs", {
        userId: userId,
        foodName: selected.name,
        weightValue: gramsNum,
        weightUnit: "g",
        calories: modalTotals.kcal,
        proteins: modalTotals.p,
        carbs: modalTotals.c,
        fats: modalTotals.f,
        mealType: null,
        loggedAt: new Date().toISOString(),
      });

      setModalOpen(false);
      setSelected(null);
      navigate("/food");
    } catch {
      setErr("Could not add food.");
    } finally {
      setSaving(false);
    }
  }

  function openManual() { // Function to open the manual entry modal, resetting all input fields and errors to their default states, allowing users to enter custom food information when they click the "Manual Entry" button, and ensuring that any previous errors or inputs do not persist when opening the modal again
    setManualErr("");
    setMName("");
    setMGrams("100");
    setMKcal("");
    setMP("");
    setMC("");
    setMF("");
    setManualOpen(true);
  }

  function closeManual() {
    if (manualSaving) return;
    setManualOpen(false);
  }

  async function confirmManual() { // Function to confirm the manual entry of a food item, validating the inputs for food name and weight, ensuring that the user is logged in before allowing the entry to be saved, and then sending a POST request to the API to create a new food entry log with the provided information, while handling loading state and errors to provide feedback to the user during the save process
    if (!userId) {
      navigate("/login");
      return;
    }

    const foodName = String(mName || "").trim();
    const g = clampNonNeg(mGrams === "" ? 0 : mGrams);
    const kcal = clampNonNeg(mKcal === "" ? 0 : mKcal);
    const p = clampNonNeg(mP === "" ? 0 : mP);
    const c = clampNonNeg(mC === "" ? 0 : mC);
    const f = clampNonNeg(mF === "" ? 0 : mF);

    if (!foodName) {
      setManualErr("Please enter a food name.");
      return;
    }
    if (g <= 0) {
      setManualErr("Please enter a valid weight.");
      return;
    }

    setManualSaving(true);
    setManualErr("");

    try { // Send a POST request to the API to create a new food entry log with the provided information from the manual entry modal, including the food name, weight, calories, protein, carbs, and fat, while ensuring that numeric inputs are validated and treated as 0 if they are not valid numbers, and providing feedback to the user during the save process by handling loading state and errors appropriately
      await apiClient.post("/api/food-entry-logs", {
        userId: userId,
        foodName,
        weightValue: roundInt(g),
        weightUnit: "g",
        calories: roundInt(kcal),
        proteins: roundInt(p),
        carbs: roundInt(c),
        fats: roundInt(f),
        mealType: null,
        loggedAt: new Date().toISOString(),
      });

      setManualOpen(false);
      navigate("/food");
    } catch {
      setManualErr("Could not add food.");
    } finally {
      setManualSaving(false);
    }
  }

  const showEmpty = tab === "Popular" ? !loading && rows.length === 0 : tab === "Recent" ? !recentLoading && recentItems.length === 0 : false;

  return (
    <div className="pageShell">
      <div className="logFoodTop">
        <button className="logFoodBackBtn" type="button" onClick={() => navigate("/food")}>
          Back
        </button>
        <div className="logFoodTitle">Log Food</div>
        <div className="logFoodTopSpacer" />
      </div>

      <div className="pageBody logFoodBody">
        <div className="logFoodSearch">
          <span className="logFoodSearchIcon">🔍</span>
          <input
            className="logFoodSearchInput"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search for Foods"
          />
          <span className="logFoodMic">🎤</span>
        </div>

        <div className="logFoodTabs">
          {["Recent", "Popular"].map((t) => (
            <button
              key={t}
              type="button"
              className={`logFoodTab ${tab === t ? "isActive" : ""}`}
              onClick={() => setTab(t)}
            >
              {t}
            </button>
          ))}
        </div>

        {err ? <div className="logFoodInlineErr">{err}</div> : null}
        {recentErr && tab === "Recent" ? <div className="logFoodInlineErr">{recentErr}</div> : null}

        <div className="logFoodTable">
          <div className="logFoodHead">
            <div>Food Item</div>
            <div className="c">Weight / Portion</div>
            <div className="r">Calories (kcal)</div>
            <div />
          </div>

          <div className="logFoodRows">
            {tab === "Popular" ? (
              loading ? (
                <div className="logFoodEmpty">Loading…</div>
              ) : showEmpty ? (
                <div className="logFoodEmpty">No results</div>
              ) : (
                rows.map((x) => (
                  <div className="logFoodRow" key={String(x.fdcId || x.name)}>
                    <div className="cell nameCell">{x.name}</div>
                    <div className="cell c">100g</div>
                    <div className="cell r">{roundInt(x.per100.kcal) || "—"}</div>
                    <button className="addBtn" type="button" aria-label={`Add ${x.name}`} onClick={() => openAdd(x)}>
                      +
                    </button>
                  </div>
                ))
              )
            ) : recentLoading ? (
              <div className="logFoodEmpty">Loading…</div>
            ) : showEmpty ? (
              <div className="logFoodEmpty">No recent foods yet</div>
            ) : (
              recentItems.map((x) => (
                <div className="logFoodRow" key={x.key}>
                  <div className="cell nameCell">{x.name}</div>
                  <div className="cell c">{x.grams ? `${x.grams}g` : "—"}</div>
                  <div className="cell r">{x.kcal ? x.kcal : "—"}</div>
                  <button className="addBtn" type="button" aria-label={`Add ${x.name}`} onClick={() => openAddFromRecent(x)}>
                    +
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="logFoodActionsOne">
          <button className="logFoodActionBtn" type="button" onClick={openManual}>
            Manual Entry
          </button>
        </div>
      </div>

      {modalOpen && selected ? (
        <div className="lfModalOverlay" role="dialog" aria-modal="true" onMouseDown={closeModal}>
          <div className="lfModalCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="lfModalTop">
              <div className="lfModalTitle">Add food</div>
              <button className="lfModalClose" type="button" onClick={closeModal} aria-label="Close">
                ✕
              </button>
            </div>

            <div className="lfModalName">{selected.name}</div>

            <div className="lfModalGrid">
              <div className="lfModalImg">
                {selected.imageUrl ? <img src={selected.imageUrl} alt="" /> : <div className="lfModalImgPh">🍽️</div>}
              </div>

              <div className="lfModalForm">
                <div className="lfField">
                  <div className="lfLabel">Weight (g)</div>
                  <input
                    className="lfInput"
                    value={grams}
                    onChange={(e) => setGrams(e.target.value)}
                    inputMode="numeric"
                    placeholder="e.g. 150"
                  />
                </div>

                <div className="lfMacros">
                  <div className="lfMacroRow">
                    <span>Calories</span>
                    <strong>{modalTotals.kcal} kcal</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Protein</span>
                    <strong>{modalTotals.p} g</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Carbs</span>
                    <strong>{modalTotals.c} g</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Fat</span>
                    <strong>{modalTotals.f} g</strong>
                  </div>
                </div>
              </div>
            </div>

            {err ? <div className="logFoodInlineErr" style={{ marginTop: 10 }}>{err}</div> : null}

            <div className="lfModalBtns">
              <button className="lfBtnSecondary" type="button" onClick={closeModal} disabled={saving}>
                Cancel
              </button>
              <button className="lfBtnPrimary" type="button" onClick={confirmAdd} disabled={saving}>
                {saving ? "Adding…" : "Add"}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {manualOpen ? (
        <div className="lfModalOverlay" role="dialog" aria-modal="true" onMouseDown={closeManual}>
          <div className="lfModalCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="lfModalTop">
              <div className="lfModalTitle">Manual entry</div>
              <button className="lfModalClose" type="button" onClick={closeManual} aria-label="Close">
                ✕
              </button>
            </div>

            {manualErr ? <div className="logFoodInlineErr">{manualErr}</div> : null}

            <div className="lfManualForm">
              <div className="lfField">
                <div className="lfLabel">Food name</div>
                <input
                  className="lfInput"
                  value={mName}
                  onChange={(e) => setMName(e.target.value)}
                  placeholder="e.g. Homemade chicken wrap"
                />
              </div>

              <div className="lfField">
                <div className="lfLabel">Weight (g)</div>
                <input
                  className="lfInput"
                  value={mGrams}
                  onChange={(e) => setMGrams(e.target.value)}
                  inputMode="numeric"
                  placeholder="e.g. 250"
                />
              </div>

              <div className="lfManualGrid">
                <div className="lfField">
                  <div className="lfLabel">Calories (kcal)</div>
                  <input className="lfInput" value={mKcal} onChange={(e) => setMKcal(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Protein (g)</div>
                  <input className="lfInput" value={mP} onChange={(e) => setMP(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Carbs (g)</div>
                  <input className="lfInput" value={mC} onChange={(e) => setMC(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Fat (g)</div>
                  <input className="lfInput" value={mF} onChange={(e) => setMF(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
              </div>

              <div className="lfManualHint">
                Tip: leave macros empty if you don’t know them — they’ll be saved as 0.
              </div>
            </div>

            <div className="lfModalBtns">
              <button className="lfBtnSecondary" type="button" onClick={closeManual} disabled={manualSaving}>
                Cancel
              </button>
              <button className="lfBtnPrimary" type="button" onClick={confirmManual} disabled={manualSaving}>
                {manualSaving ? "Adding…" : "Add"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
