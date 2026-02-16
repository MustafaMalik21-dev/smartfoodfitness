import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { useAuth } from "../auth/useAuth";
import "./LogFood.css";

function n(v) {
  const x = Number(v);
  return Number.isFinite(x) ? x : 0;
}

function roundInt(v) {
  const x = Number(v);
  if (!Number.isFinite(x)) return 0;
  return Math.round(x);
}

function calcFromPer100(per100, grams) {
  return (n(per100) * n(grams)) / 100;
}

export default function LogFood() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const userId = auth ? auth.userId : null;

  const [tab, setTab] = useState("Popular");
  const [q, setQ] = useState("");

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const [items, setItems] = useState([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [grams, setGrams] = useState("100");
  const [saving, setSaving] = useState(false);

  const query = useMemo(() => q.trim(), [q]);

  useEffect(() => {
    let cancelled = false;

    async function run() {
      setErr("");
      setLoading(true);
      try {
        const res = await apiClient.get("/api/food-database/search", {
          params: { q: query || "chicken", limit: 25 },
        });

        const next = res && res.data && res.data.items ? res.data.items : [];
        if (!cancelled) setItems(next);
      } catch (e) {
        if (!cancelled) setErr("Failed to load foods.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    if (tab === "Popular") run();
    else setItems([]);

    return () => {
      cancelled = true;
    };
  }, [query, tab]);

  const rows = useMemo(() => {
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

  const gramsNum = useMemo(() => {
    const g = Number(String(grams).trim());
    if (!Number.isFinite(g)) return 0;
    if (g < 1) return 0;
    return g;
  }, [grams]);

  const modalTotals = useMemo(() => {
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

  function closeModal() {
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
    } catch (e) {
      setErr("Could not add food.");
    } finally {
      setSaving(false);
    }
  }

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
          {["Recent", "Favorites", "Popular"].map((t) => (
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

        <div className="logFoodTable">
          <div className="logFoodHead">
            <div>Food Item</div>
            <div className="c">Weight / Portion</div>
            <div className="r">Calories (kcal)</div>
            <div />
          </div>

          <div className="logFoodRows">
            {loading ? (
              <div className="logFoodEmpty">Loading…</div>
            ) : tab !== "Popular" ? (
              <div className="logFoodEmpty">{tab} coming soon</div>
            ) : rows.length === 0 ? (
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
            )}
          </div>
        </div>

        <div className="logFoodActions">
          <button className="logFoodActionBtn" type="button">
            Scan<br />Bar-code
          </button>
          <button className="logFoodActionBtn" type="button">
            Recent<br />Foods
          </button>
          <button className="logFoodActionBtn" type="button">
            Manual<br />Entry
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
                {selected.imageUrl ? (
                  <img src={selected.imageUrl} alt="" />
                ) : (
                  <div className="lfModalImgPh">🍽️</div>
                )}
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
    </div>
  );
}
